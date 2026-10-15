package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.Constituency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConstituencyRepository extends JpaRepository<Constituency, UUID> {

    Optional<Constituency> findBySourceIdAndExternalId(UUID sourceId, String externalId);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO constituency (source_id, external_id, term_id, name, region, department_code, department_name,
                                      number, valid_from, valid_to)
            VALUES (:sourceId, :externalId,
                    (SELECT id FROM term WHERE chamber_id = :chamberId AND number = :legislature),
                    :name, :region, :departmentCode, :departmentName, :number, :validFrom, :validTo)
            ON CONFLICT (source_id, external_id) DO UPDATE
            SET term_id = EXCLUDED.term_id, name = EXCLUDED.name, region = EXCLUDED.region,
                department_code = EXCLUDED.department_code, department_name = EXCLUDED.department_name,
                number = EXCLUDED.number, valid_from = EXCLUDED.valid_from, valid_to = EXCLUDED.valid_to
            """, nativeQuery = true)
    void upsert(@Param("sourceId") UUID sourceId, @Param("externalId") String externalId, @Param("chamberId") UUID chamberId,
                @Param("legislature") Integer legislature, @Param("name") String name, @Param("region") String region,
                @Param("departmentCode") String departmentCode, @Param("departmentName") String departmentName,
                @Param("number") Integer number, @Param("validFrom") LocalDate validFrom, @Param("validTo") LocalDate validTo);

}
