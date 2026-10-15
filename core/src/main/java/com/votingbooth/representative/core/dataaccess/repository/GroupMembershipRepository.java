package com.votingbooth.representative.core.dataaccess.repository;

import com.votingbooth.representative.core.model.entity.GroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, UUID> {

    List<GroupMembership> findByPersonIdOrderByStartDateDesc(UUID personId);

    List<GroupMembership> findByGroupIdAndEndDateIsNull(UUID groupId);

}
