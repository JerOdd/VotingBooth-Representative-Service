package com.votingbooth.representative.job.utils;

import com.votingbooth.representative.job.model.amo.AmoEntity;
import com.votingbooth.representative.job.model.amo.AmoEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class AmoArchiveReaderTests {

    @TempDir
    Path tempDir;

    private AmoArchiveReader reader;

    @BeforeEach
    void setUp() {
        reader = new AmoArchiveReader();
    }

    @Test
    void readsOnlyTheRequestedEntity() throws IOException {
        Path archive = zip(Map.of(
                "json/acteur/PA1.json", "{\"acteur\":1}",
                "json/acteur/PA2.json", "{\"acteur\":2}",
                "json/organe/PO1.json", "{\"organe\":1}",
                "json/acteur/README.txt", "not json",
                "json/acteur/nested/PA3.json", "{}"
        ));

        List<AmoEntry> entries = new ArrayList<>();
        reader.forEach(archive, AmoEntity.ACTEUR, entries::add);

        assertEquals(List.of("PA1", "PA2"), entries.stream().map(AmoEntry::externalId).sorted().toList());
        AmoEntry first = entries.stream().filter(e -> e.externalId().equals("PA1")).findFirst().orElseThrow();
        assertEquals(AmoEntity.ACTEUR, first.entity());
        assertEquals("{\"acteur\":1}", new String(first.content(), StandardCharsets.UTF_8));
    }

    @Test
    void missingArchive() {
        assertThrows(RuntimeException.class,
                () -> reader.forEach(tempDir.resolve("missing.zip"), AmoEntity.ACTEUR, entry -> { }));
    }

    private Path zip(Map<String, String> entries) throws IOException {
        Path archive = tempDir.resolve("amo.zip");
        try (OutputStream out = Files.newOutputStream(archive); ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return archive;
    }

}
