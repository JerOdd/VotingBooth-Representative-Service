package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.Term;
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
public interface TermRepository extends JpaRepository<Term, UUID> {

    Optional<Term> findBySourceIdAndExternalId(UUID sourceId, String externalId);

    Optional<Term> findByChamberIdAndNumber(UUID chamberId, Integer number);

    Optional<Term> findByChamberIdAndEndDateIsNull(UUID chamberId);

    List<Term> findBySourceId(UUID sourceId);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO term (source_id, external_id, chamber_id, number, start_date, end_date)
            VALUES (:sourceId, :externalId, :chamberId, :number, :startDate, :endDate)
            ON CONFLICT (source_id, external_id) DO UPDATE
            SET chamber_id = EXCLUDED.chamber_id, number = EXCLUDED.number,
                start_date = EXCLUDED.start_date, end_date = EXCLUDED.end_date
            """, nativeQuery = true)
    void upsert(@Param("sourceId") UUID sourceId, @Param("externalId") String externalId, @Param("chamberId") UUID chamberId,
                @Param("number") Integer number, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
