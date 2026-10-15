package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.RawRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RawRecordRepository extends JpaRepository<RawRecord, UUID> {

    // Loaded once per entity at the start of a run, so per-record change detection doesn't query per file.
    List<RawRecord> findBySourceIdAndEntity(UUID sourceId, String entity);

}
