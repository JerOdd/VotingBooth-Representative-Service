package com.votingbooth.representative.api.model.dto;

import java.util.List;

public record RepresentativesResponse(List<RepresentativeSummary> representatives) {
}
