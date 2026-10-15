package com.votingbooth.representative.api.dataaccess.repository;

import com.votingbooth.representative.api.model.entity.Representative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RepresentativeRepository extends JpaRepository<Representative, UUID> { }
