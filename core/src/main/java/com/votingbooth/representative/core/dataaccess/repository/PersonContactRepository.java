package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.PersonContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface PersonContactRepository extends JpaRepository<PersonContact, UUID> {

    List<PersonContact> findByPersonIdOrderByPosition(UUID personId);

    // Contacts are replaced as a whole when the person changes: delete, then insert.
    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM person_contact
            WHERE person_id = (SELECT id FROM person WHERE source_id = :sourceId AND external_id = :personExternalId)
            """, nativeQuery = true)
    void deleteForPerson(@Param("sourceId") UUID sourceId, @Param("personExternalId") String personExternalId);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO person_contact (person_id, type, value, position)
            VALUES ((SELECT id FROM person WHERE source_id = :sourceId AND external_id = :personExternalId),
                    :type, :value, :position)
            """, nativeQuery = true)
    void insert(@Param("sourceId") UUID sourceId, @Param("personExternalId") String personExternalId,
                @Param("type") String type, @Param("value") String value, @Param("position") Integer position);

}
