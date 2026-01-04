package com.votingbooth.representativemodule.api.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RepresentativeNotFoundException extends RuntimeException {
    public RepresentativeNotFoundException(UUID id) {
        super("Representative not found: " + id);
    }
}
