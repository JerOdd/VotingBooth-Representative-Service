package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TermRepository extends JpaRepository<Term, UUID> {

    Optional<Term> findBySourceIdAndExternalId(UUID sourceId, String externalId);

    Optional<Term> findByChamberIdAndNumber(UUID chamberId, Integer number);

    Optional<Term> findByChamberIdAndEndDateIsNull(UUID chamberId);

}
