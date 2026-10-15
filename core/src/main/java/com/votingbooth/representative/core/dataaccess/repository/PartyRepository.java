package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartyRepository extends JpaRepository<Party, UUID> {

    Optional<Party> findBySourceIdAndExternalId(UUID sourceId, String externalId);

    // european_family isn't in the source, so the upsert leaves it alone.
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO party (source_id, external_id, jurisdiction_id, name, short_name)
            VALUES (:sourceId, :externalId, :jurisdictionId, :name, :shortName)
            ON CONFLICT (source_id, external_id) DO UPDATE
            SET jurisdiction_id = EXCLUDED.jurisdiction_id, name = EXCLUDED.name, short_name = EXCLUDED.short_name
            """, nativeQuery = true)
    void upsert(@Param("sourceId") UUID sourceId, @Param("externalId") String externalId,
                @Param("jurisdictionId") UUID jurisdictionId, @Param("name") String name, @Param("shortName") String shortName);

}
