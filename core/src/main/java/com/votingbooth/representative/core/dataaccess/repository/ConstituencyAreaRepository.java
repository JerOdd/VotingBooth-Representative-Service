package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.ConstituencyArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConstituencyAreaRepository extends JpaRepository<ConstituencyArea, UUID> {

    List<ConstituencyArea> findByPostcode(String postcode);

}
