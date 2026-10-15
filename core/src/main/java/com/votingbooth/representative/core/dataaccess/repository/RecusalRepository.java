package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.Recusal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecusalRepository extends JpaRepository<Recusal, UUID> {

    List<Recusal> findByPersonIdOrderByPublishedAtDesc(UUID personId);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO recusal (source_id, external_id, person_id, term_id, scope, instance, reading, target_type,
                                 target_description, explanation, published_at)
            VALUES (:sourceId, :externalId,
                    (SELECT id FROM person WHERE source_id = :sourceId AND external_id = :personExternalId),
                    (SELECT id FROM term WHERE chamber_id = :chamberId AND number = :legislature),
                    :scope, :instance, :reading, :targetType, :targetDescription, :explanation, :publishedAt)
            ON CONFLICT (source_id, external_id) DO UPDATE
            SET person_id = EXCLUDED.person_id, term_id = EXCLUDED.term_id, scope = EXCLUDED.scope,
                instance = EXCLUDED.instance, reading = EXCLUDED.reading, target_type = EXCLUDED.target_type,
                target_description = EXCLUDED.target_description, explanation = EXCLUDED.explanation,
                published_at = EXCLUDED.published_at
            """, nativeQuery = true)
    void upsert(@Param("sourceId") UUID sourceId, @Param("externalId") String externalId,
                @Param("personExternalId") String personExternalId, @Param("chamberId") UUID chamberId,
                @Param("legislature") Integer legislature, @Param("scope") String scope, @Param("instance") String instance,
                @Param("reading") String reading, @Param("targetType") String targetType,
                @Param("targetDescription") String targetDescription, @Param("explanation") String explanation,
                @Param("publishedAt") LocalDateTime publishedAt);

}
