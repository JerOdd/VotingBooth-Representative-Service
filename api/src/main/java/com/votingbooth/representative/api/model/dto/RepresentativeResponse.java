package com.votingbooth.representative.api.model.dto;

import java.util.UUID;

public record RepresentativeResponse(UUID id, String civility, String firstName, String lastName) {
}
