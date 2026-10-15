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
import com.votingbooth.representative.job.utils.AmoArchiveReader;
import com.votingbooth.representative.job.utils.AmoFixtures;
import com.votingbooth.representative.job.utils.AmoParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AmoSyncServiceTests {

    private static final String URL = "https://example.test/amo30.zip";
    private static final UUID SOURCE_ID = UUID.randomUUID();
    private static final UUID JURISDICTION_ID = UUID.randomUUID();
    private static final UUID CHAMBER_ID = UUID.randomUUID();
    private static final LocalDateTime LAST_MODIFIED = LocalDateTime.of(2026, 10, 15, 0, 34, 48);

    @TempDir
    Path tempDir;

    private AmoArchiveClient archiveClient;
    private RawStorageClient rawStorageClient;
    private AmoArchiveReader archiveReader;
    private OrganeSyncService organeSyncService;
    private ActeurSyncService acteurSyncService;
    private DeportSyncService deportSyncService;
    private TermRepository termRepository;
    private MandateRepository mandateRepository;
    private SyncRunRepository syncRunRepository;
    private RawRecordRepository rawRecordRepository;
    private AmoSyncService amoSyncService;
    private final Map<AmoEntity, List<AmoEntry>> archiveEntries = new EnumMap<>(AmoEntity.class);

    @BeforeEach
    void setUp() {
        archiveClient = mock(AmoArchiveClient.class);
        rawStorageClient = mock(RawStorageClient.class);
        archiveReader = mock(AmoArchiveReader.class);
        organeSyncService = mock(OrganeSyncService.class);
        acteurSyncService = mock(ActeurSyncService.class);
        deportSyncService = mock(DeportSyncService.class);
        DataSourceRepository dataSourceRepository = mock(DataSourceRepository.class);
        ChamberRepository chamberRepository = mock(ChamberRepository.class);
        termRepository = mock(TermRepository.class);
        mandateRepository = mock(MandateRepository.class);
        syncRunRepository = mock(SyncRunRepository.class);
        rawRecordRepository = mock(RawRecordRepository.class);

        DataSource source = mock(DataSource.class);
        when(source.getId()).thenReturn(SOURCE_ID);
        when(source.getCode()).thenReturn("assemblee-nationale");
        when(source.getJurisdictionId()).thenReturn(JURISDICTION_ID);
        when(dataSourceRepository.findByCode("assemblee-nationale")).thenReturn(Optional.of(source));
        Chamber chamber = mock(Chamber.class);
        when(chamber.getId()).thenReturn(CHAMBER_ID);
        when(chamberRepository.findByJurisdictionIdAndCode(JURISDICTION_ID, "AN")).thenReturn(Optional.of(chamber));
        Term term = mock(Term.class);
        when(term.getNumber()).thenReturn(17);
        when(termRepository.findBySourceId(SOURCE_ID)).thenReturn(List.of(term));
        when(rawStorageClient.isVersioningEnabled()).thenReturn(true);
        when(syncRunRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(syncRunRepository.findFirstBySourceIdAndArchiveUrlAndStatusInOrderByStartedAtDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        doAnswer(invocation -> {
            AmoEntity entity = invocation.getArgument(1);
            Consumer<AmoEntry> consumer = invocation.getArgument(2);
            archiveEntries.getOrDefault(entity, List.of()).forEach(consumer);
            return null;
        }).when(archiveReader).forEach(any(), any(), any());

        amoSyncService = new AmoSyncService(archiveClient, rawStorageClient, archiveReader, new AmoParser(),
                organeSyncService, acteurSyncService, deportSyncService, dataSourceRepository, chamberRepository,
                termRepository, mandateRepository, syncRunRepository, rawRecordRepository,
                Clock.fixed(Instant.parse("2026-10-15T10:00:00Z"), ZoneOffset.UTC), URL, true);
    }

    @Test
    void versioningDisabled() {
        when(rawStorageClient.isVersioningEnabled()).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> amoSyncService.sync(SyncTrigger.SCHEDULED, false));
        verify(syncRunRepository, never()).save(any());
    }

    @Test
    void archiveNotModified() {
        givenPreviousRun("\"etag-1\"", "previous-sha");
        when(archiveClient.download(URL, "\"etag-1\"", LAST_MODIFIED)).thenReturn(AmoArchiveDownload.notModified());

        SyncRun run = amoSyncService.sync(SyncTrigger.SCHEDULED, false);

        assertEquals(SyncStatus.SKIPPED, run.getStatus());
        assertEquals("\"etag-1\"", run.getArchiveEtag());
        assertEquals("previous-sha", run.getArchiveSha256());
        verifyNoInteractions(archiveReader);
    }

    @Test
    void archiveContentUnchanged() throws IOException {
        Path archive = givenDownload("same content");
        givenPreviousRun("\"etag-1\"", sha256("same content".getBytes(StandardCharsets.UTF_8)));

        SyncRun run = amoSyncService.sync(SyncTrigger.SCHEDULED, false);

        assertEquals(SyncStatus.SKIPPED, run.getStatus());
        assertEquals("\"etag-2\"", run.getArchiveEtag());
        verifyNoInteractions(archiveReader);
        assertFalse(Files.exists(archive));
    }

    @Test
    void syncsChangedRecordsInOrder() throws IOException {
        Path archive = givenDownload("new content");
        givenEntries(AmoEntity.ORGANE, "PO845401", "PO838901");
        givenEntries(AmoEntity.ACTEUR, "PA794750", "PA720888");
        givenKnownHash(AmoEntity.ACTEUR, "PA720888");

        SyncRun run = amoSyncService.sync(SyncTrigger.SCHEDULED, false);

        assertEquals(SyncStatus.SUCCEEDED, run.getStatus());
        assertEquals(3, run.getCreated());
        assertEquals(0, run.getUpdated());
        assertEquals(1, run.getUnchanged());
        assertEquals(0, run.getFailed());
        // The legislature is synced first, before its terms are known; the group after, once they are.
        InOrder inOrder = inOrder(organeSyncService, acteurSyncService, mandateRepository);
        inOrder.verify(organeSyncService).sync(argThat(c -> c.legislatures().isEmpty()), argThat(o -> o.uid().equals("PO838901")));
        inOrder.verify(organeSyncService).sync(argThat(c -> c.legislatures().equals(Set.of(17))), argThat(o -> o.uid().equals("PO845401")));
        inOrder.verify(acteurSyncService).sync(any(), argThat(a -> a.uid().equals("PA794750")));
        inOrder.verify(mandateRepository).resolveReplacedMandates(SOURCE_ID);
        verify(acteurSyncService, times(1)).sync(any(), any());
        verify(rawStorageClient).put("assemblee-nationale/acteur/PA794750.json", AmoFixtures.bytes("acteur/PA794750.json"));
        verify(rawRecordRepository).upsert(eq(SOURCE_ID), eq("acteur"), eq("PA794750"),
                eq(sha256(AmoFixtures.bytes("acteur/PA794750.json"))), eq("assemblee-nationale/acteur/PA794750.json"),
                any(), eq(run.getId()), any());
        verify(rawRecordRepository).markSeen(SOURCE_ID, "acteur", run.getId(), List.of("PA720888"));
        assertEquals("\"etag-2\"", run.getArchiveEtag());
        assertFalse(Files.exists(archive));
    }

    @Test
    void recordFailureIsCountedAndTheRunContinues() throws IOException {
        givenDownload("new content");
        givenEntries(AmoEntity.ACTEUR, "PA794750", "PA720888");
        doThrow(new IllegalStateException("person_id cannot be null"))
                .when(acteurSyncService).sync(any(), argThat(a -> a.uid().equals("PA794750")));

        SyncRun run = amoSyncService.sync(SyncTrigger.SCHEDULED, false);

        assertEquals(SyncStatus.SUCCEEDED, run.getStatus());
        assertEquals(1, run.getCreated());
        assertEquals(1, run.getFailed());
        verify(rawRecordRepository, never()).upsert(any(), any(), eq("PA794750"), any(), any(), any(), any(), any());
    }

    @Test
    void storageFailureFailsTheRun() throws IOException {
        givenDownload("new content");
        givenEntries(AmoEntity.ACTEUR, "PA794750", "PA720888");
        when(rawStorageClient.put(any(), any())).thenThrow(new IllegalStateException("bucket unreachable"));

        assertThrows(IllegalStateException.class, () -> amoSyncService.sync(SyncTrigger.SCHEDULED, false));

        verifyNoInteractions(acteurSyncService);
        verify(syncRunRepository, atLeastOnce()).save(argThat(run ->
                run.getStatus() == SyncStatus.FAILED && run.getError().contains("bucket unreachable")));
    }

    @Test
    void fatalErrorFailsTheRun() {
        when(archiveClient.download(any(), any(), any())).thenThrow(new IllegalStateException("HTTP 503"));

        assertThrows(IllegalStateException.class, () -> amoSyncService.sync(SyncTrigger.SCHEDULED, false));

        verify(syncRunRepository, atLeastOnce()).save(argThat(run ->
                run.getStatus() == SyncStatus.FAILED && run.getError().contains("HTTP 503")));
    }

    @Test
    void forceIgnoresTheArchiveValidatorsAndStoredHashes() throws IOException {
        givenPreviousRun("\"etag-1\"", "previous-sha");
        givenDownload("new content");
        givenEntries(AmoEntity.ACTEUR, "PA720888");
        givenKnownHash(AmoEntity.ACTEUR, "PA720888");

        SyncRun run = amoSyncService.sync(SyncTrigger.MANUAL, true);

        verify(archiveClient).download(URL, null, null);
        verify(acteurSyncService).sync(any(), argThat(a -> a.uid().equals("PA720888")));
        assertEquals(1, run.getUpdated());
        assertEquals(0, run.getUnchanged());
    }

    private void givenPreviousRun(String etag, String sha256) {
        SyncRun previous = mock(SyncRun.class);
        when(previous.getArchiveEtag()).thenReturn(etag);
        when(previous.getArchiveLastModified()).thenReturn(LAST_MODIFIED);
        when(previous.getArchiveSha256()).thenReturn(sha256);
        when(syncRunRepository.findFirstBySourceIdAndArchiveUrlAndStatusInOrderByStartedAtDesc(
                SOURCE_ID, URL, List.of(SyncStatus.SUCCEEDED, SyncStatus.SKIPPED))).thenReturn(Optional.of(previous));
    }

    private Path givenDownload(String content) throws IOException {
        Path archive = Files.writeString(tempDir.resolve("amo.zip"), content);
        when(archiveClient.download(eq(URL), any(), any()))
                .thenReturn(new AmoArchiveDownload(true, archive, "\"etag-2\"", LAST_MODIFIED.plusDays(1)));
        return archive;
    }

    private void givenEntries(AmoEntity entity, String... uids) {
        archiveEntries.put(entity, List.of(uids).stream()
                .map(uid -> new AmoEntry(entity, uid, AmoFixtures.bytes(entity.directory() + "/" + uid + ".json")))
                .toList());
    }

    private void givenKnownHash(AmoEntity entity, String uid) {
        RawRecord rawRecord = mock(RawRecord.class);
        when(rawRecord.getExternalId()).thenReturn(uid);
        when(rawRecord.getSha256()).thenReturn(sha256(AmoFixtures.bytes(entity.directory() + "/" + uid + ".json")));
        when(rawRecordRepository.findBySourceIdAndEntity(SOURCE_ID, entity.directory())).thenReturn(List.of(rawRecord));
    }

    private static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
