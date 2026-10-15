package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonRepository extends JpaRepository<Person, UUID> {

    Optional<Person> findBySourceIdAndExternalId(UUID sourceId, String externalId);

    // photo_url isn't in the source, so the upsert leaves it alone.
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO person (source_id, external_id, civility, first_name, last_name, sort_name, birth_date, birth_place,
                                death_date, profession, profession_category, transparency_url, updated_at)
            VALUES (:sourceId, :externalId, :civility, :firstName, :lastName, :sortName, :birthDate, :birthPlace,
                    :deathDate, :profession, :professionCategory, :transparencyUrl, :updatedAt)
            ON CONFLICT (source_id, external_id) DO UPDATE
            SET civility = EXCLUDED.civility, first_name = EXCLUDED.first_name, last_name = EXCLUDED.last_name,
                sort_name = EXCLUDED.sort_name, birth_date = EXCLUDED.birth_date, birth_place = EXCLUDED.birth_place,
                death_date = EXCLUDED.death_date, profession = EXCLUDED.profession,
                profession_category = EXCLUDED.profession_category, transparency_url = EXCLUDED.transparency_url,
                updated_at = EXCLUDED.updated_at
            """, nativeQuery = true)
    void upsert(@Param("sourceId") UUID sourceId, @Param("externalId") String externalId, @Param("civility") String civility,
                @Param("firstName") String firstName, @Param("lastName") String lastName, @Param("sortName") String sortName,
                @Param("birthDate") LocalDate birthDate, @Param("birthPlace") String birthPlace,
                @Param("deathDate") LocalDate deathDate, @Param("profession") String profession,
                @Param("professionCategory") String professionCategory, @Param("transparencyUrl") String transparencyUrl,
                @Param("updatedAt") LocalDateTime updatedAt);

}
