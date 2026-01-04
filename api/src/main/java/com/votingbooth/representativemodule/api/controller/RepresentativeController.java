package com.votingbooth.representativemodule.api.controller;

import com.votingbooth.representativemodule.api.model.Representative;
import com.votingbooth.representativemodule.api.model.RepresentativeResponse;
import com.votingbooth.representativemodule.api.service.RepresentativeService;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class RepresentativeController {
    @Autowired
    private RepresentativeService representativeService;

    @GetMapping(path = "/representatives/{id}")
    public ResponseEntity<RepresentativeResponse> getRepresentative(@PathVariable UUID id) {
        RepresentativeResponse representativeResponse = representativeService.getRepresentative(id);
        return ResponseEntity.ok(representativeResponse);
    }
}
