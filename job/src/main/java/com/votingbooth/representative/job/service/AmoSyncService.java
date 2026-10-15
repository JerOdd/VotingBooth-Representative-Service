package com.votingbooth.representative.job.service;

import com.votingbooth.representative.core.dataaccess.repository.ChamberRepository;
import com.votingbooth.representative.core.dataaccess.repository.DataSourceRepository;
import com.votingbooth.representative.core.dataaccess.repository.MandateRepository;
import com.votingbooth.representative.core.dataaccess.repository.RawRecordRepository;
import com.votingbooth.representative.core.dataaccess.repository.SyncRunRepository;
import com.votingbooth.representative.core.dataaccess.repository.TermRepository;
import com.votingbooth.representative.core.model.entity.Chamber;
import com.votingbooth.representative.core.model.entity.DataSource;
import com.votingbooth.representative.core.model.entity.RawRecord;
import com.votingbooth.representative.core.model.entity.SyncRun;
import com.votingbooth.representative.core.model.entity.SyncStatus;
import com.votingbooth.representative.core.model.entity.SyncTrigger;
import com.votingbooth.representative.core.model.entity.Term;
import com.votingbooth.representative.job.dataaccess.client.assembleenationale.AmoArchiveClient;
import com.votingbooth.representative.job.dataaccess.client.storage.RawStorageClient;
import com.votingbooth.representative.job.model.amo.AmoArchiveDownload;
import com.votingbooth.representative.job.model.amo.AmoEntity;
import com.votingbooth.representative.job.model.amo.AmoEntry;
import com.votingbooth.representative.job.model.amo.Organe;
import com.votingbooth.representative.job.model.sync.ChangedOrgane;
import com.votingbooth.representative.job.model.sync.SyncContext;
import com.votingbooth.representative.job.model.sync.SyncCounts;
import com.votingbooth.representative.job.utils.AmoArchiveReader;
import com.votingbooth.representative.job.utils.AmoParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AmoSyncService {

    private static final Logger log = LoggerFactory.getLogger(AmoSyncService.class);

    static final String SOURCE = "assemblee-nationale";
    static final String CHAMBER = "AN";
    private static final int MARK_SEEN_BATCH = 1000;

    private final AmoArchiveClient archiveClient;
    private final RawStorageClient rawStorageClient;
    private final AmoArchiveReader archiveReader;
    private final AmoParser parser;
    private final OrganeSyncService organeSyncService;
    private final ActeurSyncService acteurSyncService;
    private final DeportSyncService deportSyncService;
    private final DataSourceRepository dataSourceRepository;
    private final ChamberRepository chamberRepository;
    private final TermRepository termRepository;
    private final MandateRepository mandateRepository;
    private final SyncRunRepository syncRunRepository;
    private final RawRecordRepository rawRecordRepository;
    private final Clock clock;
    private final String archiveUrl;
    private final boolean requireVersioning;

    public AmoSyncService(AmoArchiveClient archiveClient, RawStorageClient rawStorageClient, AmoArchiveReader archiveReader,
                          AmoParser parser, OrganeSyncService organeSyncService, ActeurSyncService acteurSyncService,
                          DeportSyncService deportSyncService, DataSourceRepository dataSourceRepository,
                          ChamberRepository chamberRepository, TermRepository termRepository,
                          MandateRepository mandateRepository, SyncRunRepository syncRunRepository,
                          RawRecordRepository rawRecordRepository, Clock clock,
                          @Value("${amo.archive-url}") String archiveUrl,
                          @Value("${storage.require-versioning:true}") boolean requireVersioning) {
        this.archiveClient = archiveClient;
        this.rawStorageClient = rawStorageClient;
        this.archiveReader = archiveReader;
        this.parser = parser;
        this.organeSyncService = organeSyncService;
        this.acteurSyncService = acteurSyncService;
        this.deportSyncService = deportSyncService;
        this.dataSourceRepository = dataSourceRepository;
        this.chamberRepository = chamberRepository;
        this.termRepository = termRepository;
        this.mandateRepository = mandateRepository;
        this.syncRunRepository = syncRunRepository;
        this.rawRecordRepository = rawRecordRepository;
        this.clock = clock;
        this.archiveUrl = archiveUrl;
        this.requireVersioning = requireVersioning;
    }

    // force ignores the archive's ETag and every stored hash, so all records are processed again (e.g. after a mapping change).
    public SyncRun sync(SyncTrigger trigger, boolean force) {
        DataSource source = dataSourceRepository.findByCode(SOURCE)
                .orElseThrow(() -> new IllegalStateException("Data source " + SOURCE + " is not seeded"));
        Chamber chamber = chamberRepository.findByJurisdictionIdAndCode(source.getJurisdictionId(), CHAMBER)
                .orElseThrow(() -> new IllegalStateException("Chamber " + CHAMBER + " is not seeded"));
        if (requireVersioning && !rawStorageClient.isVersioningEnabled()) {
            throw new IllegalStateException("Bucket versioning is off, so raw files would be overwritten. "
                    + "Enable it on the bucket, or set storage.require-versioning=false for a local run.");
        }

        Optional<SyncRun> previous = force
                ? Optional.empty()
                : syncRunRepository.findFirstBySourceIdAndArchiveUrlAndStatusInOrderByStartedAtDesc(
                        source.getId(), archiveUrl, List.of(SyncStatus.SUCCEEDED, SyncStatus.SKIPPED));
        SyncRun run = syncRunRepository.save(new SyncRun(source.getId(), trigger, archiveUrl, now()));
        log.info("Sync run {} started for {} (force: {})", run.getId(), archiveUrl, force);

        Path archive = null;
        try {
            AmoArchiveDownload download = archiveClient.download(archiveUrl,
                    previous.map(SyncRun::getArchiveEtag).orElse(null),
                    previous.map(SyncRun::getArchiveLastModified).orElse(null));
            if (!download.modified()) {
                SyncRun last = previous.orElseThrow();
                run.recordArchive(last.getArchiveEtag(), last.getArchiveLastModified(), last.getArchiveSha256());
                return skip(run, "archive not modified since the last run");
            }
            archive = download.file();
            String sha256 = sha256(archive);
            run.recordArchive(download.etag(), download.lastModified(), sha256);
            // The archive is regenerated every night, often with identical content.
            if (previous.isPresent() && sha256.equals(previous.get().getArchiveSha256())) {
                return skip(run, "archive content unchanged");
            }

            SyncContext context = new SyncContext(source.getId(), source.getCode(), source.getJurisdictionId(),
                    chamber.getId(), run.getId(), Set.of(), force);
            SyncCounts counts = new SyncCounts();
            context = syncOrganes(archive, context, counts);
            syncActeurs(archive, context, counts);
            syncDeports(archive, context, counts);
            mandateRepository.resolveReplacedMandates(source.getId());

            run.succeed(counts.getCreated(), counts.getUpdated(), counts.getUnchanged(), counts.getFailed(), now());
            log.info("Sync run {} succeeded: {} created, {} updated, {} unchanged, {} failed", run.getId(),
                    counts.getCreated(), counts.getUpdated(), counts.getUnchanged(), counts.getFailed());
            return syncRunRepository.save(run);
        } catch (RuntimeException e) {
            run.fail(e.toString(), now());
            syncRunRepository.save(run);
            log.error("Sync run {} failed", run.getId(), e);
            throw e;
        } finally {
            deleteQuietly(archive);
        }
    }

    // Two passes over organes: legislatures first, because groups and constituencies (and later mandates) attach to them.
    private SyncContext syncOrganes(Path archive, SyncContext context, SyncCounts counts) {
        Map<String, String> knownHashes = knownHashes(context, AmoEntity.ORGANE);
        List<String> unchanged = new ArrayList<>();
        List<ChangedOrgane> legislatures = new ArrayList<>();
        List<ChangedOrgane> others = new ArrayList<>();
        archiveReader.forEach(archive, AmoEntity.ORGANE, entry -> {
            String sha256 = sha256(entry.content());
            if (isUnchanged(context, knownHashes, entry, sha256)) {
                unchanged.add(entry.externalId());
                return;
            }
            try {
                Organe organe = parser.parseOrgane(entry.content());
                (OrganeSyncService.isLegislature(organe) ? legislatures : others).add(new ChangedOrgane(entry, sha256, organe));
            } catch (RuntimeException e) {
                recordFailure(counts, entry, e);
            }
        });
        SyncContext withoutTerms = context;
        legislatures.forEach(changed -> apply(withoutTerms, counts, knownHashes, changed.entry(), changed.sha256(),
                () -> organeSyncService.sync(withoutTerms, changed.organe())));

        SyncContext withTerms = context.withLegislatures(termRepository.findBySourceId(context.sourceId()).stream()
                .map(Term::getNumber)
                .collect(Collectors.toUnmodifiableSet()));
        others.forEach(changed -> apply(withTerms, counts, knownHashes, changed.entry(), changed.sha256(),
                () -> organeSyncService.sync(withTerms, changed.organe())));
        markSeen(context, AmoEntity.ORGANE, unchanged, counts);
        return withTerms;
    }

    private void syncActeurs(Path archive, SyncContext context, SyncCounts counts) {
        Map<String, String> knownHashes = knownHashes(context, AmoEntity.ACTEUR);
        List<String> unchanged = new ArrayList<>();
        archiveReader.forEach(archive, AmoEntity.ACTEUR, entry -> {
            String sha256 = sha256(entry.content());
            if (isUnchanged(context, knownHashes, entry, sha256)) {
                unchanged.add(entry.externalId());
                return;
            }
            apply(context, counts, knownHashes, entry, sha256,
                    () -> acteurSyncService.sync(context, parser.parseActeur(entry.content())));
        });
        markSeen(context, AmoEntity.ACTEUR, unchanged, counts);
    }

    private void syncDeports(Path archive, SyncContext context, SyncCounts counts) {
        Map<String, String> knownHashes = knownHashes(context, AmoEntity.DEPORT);
        List<String> unchanged = new ArrayList<>();
        archiveReader.forEach(archive, AmoEntity.DEPORT, entry -> {
            String sha256 = sha256(entry.content());
            if (isUnchanged(context, knownHashes, entry, sha256)) {
                unchanged.add(entry.externalId());
                return;
            }
            apply(context, counts, knownHashes, entry, sha256,
                    () -> deportSyncService.sync(context, parser.parseDeport(entry.content())));
        });
        markSeen(context, AmoEntity.DEPORT, unchanged, counts);
    }

    // Stores the raw file, syncs the tables, then records the hash. A failure leaves the old hash, so the record is retried next run.
    // A storage error isn't about the record: it propagates and fails the run, instead of failing every record one by one.
    private void apply(SyncContext context, SyncCounts counts, Map<String, String> knownHashes, AmoEntry entry,
                       String sha256, Runnable syncTables) {
        String key = context.sourceCode() + "/" + entry.entity().directory() + "/" + entry.externalId() + ".json";
        String versionId = rawStorageClient.put(key, entry.content());
        try {
            syncTables.run();
            rawRecordRepository.upsert(context.sourceId(), entry.entity().directory(), entry.externalId(), sha256,
                    key, versionId, context.runId(), now());
            if (knownHashes.containsKey(entry.externalId())) {
                counts.updated();
            } else {
                counts.created();
            }
        } catch (RuntimeException e) {
            recordFailure(counts, entry, e);
        }
    }

    private boolean isUnchanged(SyncContext context, Map<String, String> knownHashes, AmoEntry entry, String sha256) {
        return !context.force() && sha256.equals(knownHashes.get(entry.externalId()));
    }

    private Map<String, String> knownHashes(SyncContext context, AmoEntity entity) {
        return rawRecordRepository.findBySourceIdAndEntity(context.sourceId(), entity.directory()).stream()
                .collect(Collectors.toMap(RawRecord::getExternalId, RawRecord::getSha256));
    }

    private void markSeen(SyncContext context, AmoEntity entity, List<String> unchanged, SyncCounts counts) {
        for (int from = 0; from < unchanged.size(); from += MARK_SEEN_BATCH) {
            List<String> batch = unchanged.subList(from, Math.min(from + MARK_SEEN_BATCH, unchanged.size()));
            rawRecordRepository.markSeen(context.sourceId(), entity.directory(), context.runId(), batch);
        }
        counts.unchanged(unchanged.size());
    }

    private static void recordFailure(SyncCounts counts, AmoEntry entry, RuntimeException e) {
        counts.failed();
        log.warn("Could not sync {} {}: {}", entry.entity().directory(), entry.externalId(), e.toString());
    }

    private SyncRun skip(SyncRun run, String reason) {
        run.skip(now());
        log.info("Sync run {} skipped: {}", run.getId(), reason);
        return syncRunRepository.save(run);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private static String sha256(byte[] content) {
        return HexFormat.of().formatHex(digest().digest(content));
    }

    private static String sha256(Path file) {
        MessageDigest digest = digest();
        try (InputStream in = new DigestInputStream(Files.newInputStream(file), digest)) {
            in.transferTo(OutputStream.nullOutputStream());
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot hash " + file, e);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Could not delete temporary archive {}", file);
        }
    }

}
