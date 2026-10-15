package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.PartyAffiliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PartyAffiliationRepository extends JpaRepository<PartyAffiliation, UUID> {

    List<PartyAffiliation> findByPersonIdOrderByStartDateDesc(UUID personId);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO party_affiliation (source_id, external_id, person_id, party_id, start_date, end_date)
            VALUES (:sourceId, :externalId,
                    (SELECT id FROM person WHERE source_id = :sourceId AND external_id = :personExternalId),
                    (SELECT id FROM party WHERE source_id = :sourceId AND external_id = :partyExternalId),
                    :startDate, :endDate)
            ON CONFLICT (source_id, external_id) DO UPDATE
            SET person_id = EXCLUDED.person_id, party_id = EXCLUDED.party_id,
                start_date = EXCLUDED.start_date, end_date = EXCLUDED.end_date
            """, nativeQuery = true)
    void upsert(@Param("sourceId") UUID sourceId, @Param("externalId") String externalId,
                @Param("personExternalId") String personExternalId, @Param("partyExternalId") String partyExternalId,
                @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
