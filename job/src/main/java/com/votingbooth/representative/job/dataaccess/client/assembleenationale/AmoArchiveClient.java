package com.votingbooth.representative.job.dataaccess.client.assembleenationale;

import com.votingbooth.representative.job.model.amo.AmoArchiveDownload;

import java.time.LocalDateTime;

public interface AmoArchiveClient {

    // Sends etag / lastModified (when known) as a conditional request, so an unchanged archive isn't downloaded.
    AmoArchiveDownload download(String url, String etag, LocalDateTime lastModified);

}
