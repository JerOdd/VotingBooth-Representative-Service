package com.votingbooth.representative.api.model.dto;

import java.util.UUID;

public record RepresentativeSummary(UUID id, String civility, String firstName, String lastName,
                                    String groupName, String groupShortName, String groupColor,
                                    String departmentCode, String departmentName, Integer constituencyNumber) {
}
