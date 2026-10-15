package com.votingbooth.representative.api.model.exception;

import java.util.UUID;

public class RepresentativeNotFoundException extends RuntimeException {
    public RepresentativeNotFoundException(UUID id) {
        super("Representative " + id + " not found.");
    }
}
