package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.Recusal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecusalRepository extends JpaRepository<Recusal, UUID> {

    List<Recusal> findByPersonIdOrderByPublishedAtDesc(UUID personId);

}
