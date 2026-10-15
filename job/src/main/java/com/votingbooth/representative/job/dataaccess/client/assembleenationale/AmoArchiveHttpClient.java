package com.votingbooth.representative.job.dataaccess.client.assembleenationale;

import com.votingbooth.representative.job.model.amo.AmoArchiveDownload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AmoArchiveHttpClient implements AmoArchiveClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Override
    public AmoArchiveDownload download(String url, String etag, LocalDateTime lastModified) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(10)).GET();
        if (etag != null) {
            request.header("If-None-Match", etag);
        }
        if (lastModified != null) {
            request.header("If-Modified-Since", DateTimeFormatter.RFC_1123_DATE_TIME.format(lastModified.atOffset(ZoneOffset.UTC)));
        }
        Path file = null;
        try {
            file = Files.createTempFile("amo-", ".zip");
            HttpResponse<Path> response = httpClient.send(request.build(), HttpResponse.BodyHandlers.ofFile(file));
            if (response.statusCode() == 304) {
                Files.deleteIfExists(file);
                return AmoArchiveDownload.notModified();
            }
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Downloading " + url + " returned HTTP " + response.statusCode());
            }
            return new AmoArchiveDownload(
                    true,
                    file,
                    response.headers().firstValue("ETag").orElse(null),
                    response.headers().firstValue("Last-Modified").map(AmoArchiveHttpClient::parseHttpDate).orElse(null)
            );
        } catch (IOException e) {
            deleteQuietly(file);
            throw new UncheckedIOException("Cannot download " + url, e);
        } catch (InterruptedException e) {
            deleteQuietly(file);
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while downloading " + url, e);
        } catch (RuntimeException e) {
            deleteQuietly(file);
            throw e;
        }
    }

    private static LocalDateTime parseHttpDate(String value) {
        return ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    private static void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // A leftover temp file isn't worth hiding the original error.
        }
    }

}
