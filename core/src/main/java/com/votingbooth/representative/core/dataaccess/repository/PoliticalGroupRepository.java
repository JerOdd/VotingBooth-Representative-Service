package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.PoliticalGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PoliticalGroupRepository extends JpaRepository<PoliticalGroup, UUID> {

    Optional<PoliticalGroup> findBySourceIdAndExternalId(UUID sourceId, String externalId);

    List<PoliticalGroup> findByTermIdOrderByName(UUID termId);

}
