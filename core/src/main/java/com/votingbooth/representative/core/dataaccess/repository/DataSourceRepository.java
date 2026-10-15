package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DataSourceRepository extends JpaRepository<DataSource, UUID> {

    Optional<DataSource> findByCode(String code);

}
