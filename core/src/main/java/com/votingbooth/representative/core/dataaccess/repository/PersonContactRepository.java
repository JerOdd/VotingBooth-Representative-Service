package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.PersonContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PersonContactRepository extends JpaRepository<PersonContact, UUID> {

    List<PersonContact> findByPersonIdOrderByPosition(UUID personId);

}
