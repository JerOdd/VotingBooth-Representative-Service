package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.Mandate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MandateRepository extends JpaRepository<Mandate, UUID> {

    Optional<Mandate> findBySourceIdAndExternalId(UUID sourceId, String externalId);

    // Batch lookup for other services resolving the seat mandates (PM…) they store on each vote.
    List<Mandate> findBySourceIdAndExternalIdIn(UUID sourceId, Collection<String> externalIds);

    List<Mandate> findByPersonIdOrderByStartDateDesc(UUID personId);

}
