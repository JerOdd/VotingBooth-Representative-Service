package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.GroupMembership;
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
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, UUID> {

    List<GroupMembership> findByPersonIdOrderByStartDateDesc(UUID personId);

    List<GroupMembership> findByGroupIdAndEndDateIsNull(UUID groupId);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO group_membership (source_id, external_id, person_id, group_id, role, start_date, end_date)
            VALUES (:sourceId, :externalId,
                    (SELECT id FROM person WHERE source_id = :sourceId AND external_id = :personExternalId),
                    (SELECT id FROM political_group WHERE source_id = :sourceId AND external_id = :groupExternalId),
                    :role, :startDate, :endDate)
            ON CONFLICT (source_id, external_id) DO UPDATE
            SET person_id = EXCLUDED.person_id, group_id = EXCLUDED.group_id, role = EXCLUDED.role,
                start_date = EXCLUDED.start_date, end_date = EXCLUDED.end_date
            """, nativeQuery = true)
    void upsert(@Param("sourceId") UUID sourceId, @Param("externalId") String externalId,
                @Param("personExternalId") String personExternalId, @Param("groupExternalId") String groupExternalId,
                @Param("role") String role, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
