package com.votingbooth.representative.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sync_run")
public class SyncRun {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger", nullable = false)
    private SyncTrigger trigger;

    @Column(name = "archive_url", nullable = false)
    private String archiveUrl;

    @Column(name = "archive_etag", nullable = true)
    private String archiveEtag;

    @Column(name = "archive_last_modified", nullable = true)
    private LocalDateTime archiveLastModified;

    @Column(name = "archive_sha256", nullable = true)
    private String archiveSha256;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at", nullable = true)
    private LocalDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SyncStatus status;

    @Column(name = "created", nullable = false)
    private Integer created;

    @Column(name = "updated", nullable = false)
    private Integer updated;

    @Column(name = "unchanged", nullable = false)
    private Integer unchanged;

    @Column(name = "failed", nullable = false)
    private Integer failed;

    @Column(name = "error", nullable = true)
    private String error;

    protected SyncRun() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public SyncTrigger getTrigger() {
        return trigger;
    }

    public String getArchiveUrl() {
        return archiveUrl;
    }

    public String getArchiveEtag() {
        return archiveEtag;
    }

    public LocalDateTime getArchiveLastModified() {
        return archiveLastModified;
    }

    public String getArchiveSha256() {
        return archiveSha256;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public SyncStatus getStatus() {
        return status;
    }

    public Integer getCreated() {
        return created;
    }

    public Integer getUpdated() {
        return updated;
    }

    public Integer getUnchanged() {
        return unchanged;
    }

    public Integer getFailed() {
        return failed;
    }

    public String getError() {
        return error;
    }
}
