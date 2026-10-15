package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.PartyAffiliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PartyAffiliationRepository extends JpaRepository<PartyAffiliation, UUID> {

    List<PartyAffiliation> findByPersonIdOrderByStartDateDesc(UUID personId);

}
