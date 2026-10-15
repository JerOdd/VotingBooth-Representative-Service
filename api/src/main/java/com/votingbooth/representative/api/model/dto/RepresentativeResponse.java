package com.votingbooth.representative.api.model.dto;

import java.util.UUID;

public record RepresentativeResponse(UUID id, String firstName, String lastName, UUID partyId) {
}
