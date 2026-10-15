package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.Jurisdiction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JurisdictionRepository extends JpaRepository<Jurisdiction, UUID> {

    Optional<Jurisdiction> findByIsoCode(String isoCode);

}
