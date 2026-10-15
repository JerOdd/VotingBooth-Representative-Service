package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.Chamber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChamberRepository extends JpaRepository<Chamber, UUID> {

    Optional<Chamber> findByJurisdictionIdAndCode(UUID jurisdictionId, String code);

}
