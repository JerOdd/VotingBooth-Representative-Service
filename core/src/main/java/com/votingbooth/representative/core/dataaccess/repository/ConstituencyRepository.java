package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.Constituency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConstituencyRepository extends JpaRepository<Constituency, UUID> {

    Optional<Constituency> findBySourceIdAndExternalId(UUID sourceId, String externalId);

}
