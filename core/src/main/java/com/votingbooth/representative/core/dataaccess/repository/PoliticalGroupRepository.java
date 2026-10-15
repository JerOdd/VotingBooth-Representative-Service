package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.PoliticalGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PoliticalGroupRepository extends JpaRepository<PoliticalGroup, UUID> {

    Optional<PoliticalGroup> findBySourceIdAndExternalId(UUID sourceId, String externalId);

    List<PoliticalGroup> findByTermIdOrderByName(UUID termId);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO political_group (source_id, external_id, term_id, name, short_name, color, start_date, end_date)
            VALUES (:sourceId, :externalId,
                    (SELECT id FROM term WHERE chamber_id = :chamberId AND number = :legislature),
                    :name, :shortName, :color, :startDate, :endDate)
            ON CONFLICT (source_id, external_id) DO UPDATE
            SET term_id = EXCLUDED.term_id, name = EXCLUDED.name, short_name = EXCLUDED.short_name, color = EXCLUDED.color,
                start_date = EXCLUDED.start_date, end_date = EXCLUDED.end_date
            """, nativeQuery = true)
    void upsert(@Param("sourceId") UUID sourceId, @Param("externalId") String externalId, @Param("chamberId") UUID chamberId,
                @Param("legislature") Integer legislature, @Param("name") String name, @Param("shortName") String shortName,
                @Param("color") String color, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
