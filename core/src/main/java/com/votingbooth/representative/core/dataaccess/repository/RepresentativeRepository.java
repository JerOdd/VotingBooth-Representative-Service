package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.Representative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RepresentativeRepository extends JpaRepository<Representative, UUID> { }
