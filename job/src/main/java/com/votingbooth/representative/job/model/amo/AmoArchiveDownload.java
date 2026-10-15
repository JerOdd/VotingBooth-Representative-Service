package com.votingbooth.representative.job.model.amo;

import java.nio.file.Path;
import java.time.LocalDateTime;

// Result of a conditional download: either "not modified" (nothing downloaded) or a temporary file with its HTTP validators.
public record AmoArchiveDownload(boolean modified, Path file, String etag, LocalDateTime lastModified) {

    public static AmoArchiveDownload notModified() {
        return new AmoArchiveDownload(false, null, null, null);
    }
}
