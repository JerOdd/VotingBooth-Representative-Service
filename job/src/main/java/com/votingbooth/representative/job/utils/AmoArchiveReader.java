package com.votingbooth.representative.job.utils;

import com.votingbooth.representative.job.model.amo.AmoEntity;
import com.votingbooth.representative.job.model.amo.AmoEntry;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
public class AmoArchiveReader {

    private static final String EXTENSION = ".json";

    // Reads one kind of entry (json/<entity>/<uid>.json) straight from the zip, without extracting it to disk.
    // Called once per kind so the sync can process organes before the acteurs that reference them.
    public void forEach(Path archive, AmoEntity entity, Consumer<AmoEntry> consumer) {
        String prefix = "json/" + entity.directory() + "/";
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            for (ZipEntry entry : Collections.list(zip.entries())) {
                String name = entry.getName();
                if (entry.isDirectory() || !name.startsWith(prefix) || !name.endsWith(EXTENSION)) {
                    continue;
                }
                String externalId = name.substring(prefix.length(), name.length() - EXTENSION.length());
                if (externalId.contains("/")) {
                    continue;
                }
                try (InputStream in = zip.getInputStream(entry)) {
                    consumer.accept(new AmoEntry(entity, externalId, in.readAllBytes()));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read AMO archive " + archive, e);
        }
    }

}
