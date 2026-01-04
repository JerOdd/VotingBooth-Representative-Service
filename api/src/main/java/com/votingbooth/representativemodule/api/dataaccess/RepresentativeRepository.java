package com.votingbooth.representativemodule.api.dataaccess;

import com.votingbooth.representativemodule.api.model.Representative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RepresentativeRepository extends JpaRepository<Representative, UUID> { }
