package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.Mandate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    // The sitting members of a chamber: open seats of its current term, with their current group and constituency.
    // Group presidents also have a PRESIDENT membership, so only MEMBER / AFFILIATED rows give the group.
    @Query(value = """
            SELECT p.id AS "personId", p.civility AS "civility", p.first_name AS "firstName", p.last_name AS "lastName",
                   g.name AS "groupName", g.short_name AS "groupShortName", g.color AS "groupColor",
                   c.department_code AS "departmentCode", c.department_name AS "departmentName",
                   c.number AS "constituencyNumber"
            FROM term t
            JOIN chamber ch ON ch.id = t.chamber_id
            JOIN jurisdiction j ON j.id = ch.jurisdiction_id
            JOIN mandate m ON m.term_id = t.id AND m.end_date IS NULL
            JOIN person p ON p.id = m.person_id
            LEFT JOIN constituency c ON c.id = m.constituency_id
            LEFT JOIN group_membership gm ON gm.person_id = p.id AND gm.end_date IS NULL
                                         AND gm.role IN ('MEMBER', 'AFFILIATED')
            LEFT JOIN political_group g ON g.id = gm.group_id
            WHERE j.iso_code = :countryCode AND ch.code = :chamberCode AND t.end_date IS NULL
            ORDER BY p.sort_name, p.first_name
            """, nativeQuery = true)
    List<CurrentSeat> findCurrentSeats(@Param("countryCode") String countryCode, @Param("chamberCode") String chamberCode);

    interface CurrentSeat {
        UUID getPersonId();

        String getCivility();

        String getFirstName();

        String getLastName();

        String getGroupName();

        String getGroupShortName();

        String getGroupColor();

        String getDepartmentCode();

        String getDepartmentName();

        Integer getConstituencyNumber();
    }

    // replaces_mandate_id is only set if the replaced mandate is already synced; resolveReplacedMandates links the rest.
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO mandate (source_id, external_id, person_id, term_id, constituency_id, start_date, end_date,
                                 start_reason, end_reason, replaces_mandate_external_id, replaces_mandate_id,
                                 seat_number, first_elected)
            VALUES (:sourceId, :externalId,
                    (SELECT id FROM person WHERE source_id = :sourceId AND external_id = :personExternalId),
                    (SELECT id FROM term WHERE source_id = :sourceId AND external_id = :termExternalId),
                    (SELECT id FROM constituency WHERE source_id = :sourceId AND external_id = :constituencyExternalId),
                    :startDate, :endDate, :startReason, :endReason, :replacesExternalId,
                    (SELECT id FROM mandate WHERE source_id = :sourceId AND external_id = :replacesExternalId),
                    :seatNumber, :firstElected)
            ON CONFLICT (source_id, external_id) DO UPDATE
            SET person_id = EXCLUDED.person_id, term_id = EXCLUDED.term_id, constituency_id = EXCLUDED.constituency_id,
                start_date = EXCLUDED.start_date, end_date = EXCLUDED.end_date, start_reason = EXCLUDED.start_reason,
                end_reason = EXCLUDED.end_reason, replaces_mandate_external_id = EXCLUDED.replaces_mandate_external_id,
                replaces_mandate_id = EXCLUDED.replaces_mandate_id, seat_number = EXCLUDED.seat_number,
                first_elected = EXCLUDED.first_elected
            """, nativeQuery = true)
    void upsert(@Param("sourceId") UUID sourceId, @Param("externalId") String externalId,
                @Param("personExternalId") String personExternalId, @Param("termExternalId") String termExternalId,
                @Param("constituencyExternalId") String constituencyExternalId, @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate, @Param("startReason") String startReason,
                @Param("endReason") String endReason, @Param("replacesExternalId") String replacesExternalId,
                @Param("seatNumber") Integer seatNumber, @Param("firstElected") Boolean firstElected);

    // Run once at the end of a sync: a replacing mandate can be synced before the one it replaces.
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE mandate m SET replaces_mandate_id = r.id
            FROM mandate r
            WHERE m.source_id = :sourceId AND m.replaces_mandate_id IS NULL
              AND r.source_id = m.source_id AND r.external_id = m.replaces_mandate_external_id
            """, nativeQuery = true)
    void resolveReplacedMandates(@Param("sourceId") UUID sourceId);

}
