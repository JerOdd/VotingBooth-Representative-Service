package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.SyncRun;
import com.votingbooth.representative.core.model.entity.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SyncRunRepository extends JpaRepository<SyncRun, UUID> {

    // The job sends this run's ETag / Last-Modified / SHA-256 back to the source to detect an unchanged archive.
    Optional<SyncRun> findFirstBySourceIdAndArchiveUrlAndStatusOrderByStartedAtDesc(UUID sourceId, String archiveUrl, SyncStatus status);

}
