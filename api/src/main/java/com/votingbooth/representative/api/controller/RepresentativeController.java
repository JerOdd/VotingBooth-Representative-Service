package com.votingbooth.representative.api.controller;

import com.votingbooth.representative.api.model.dto.RepresentativeResponse;
import com.votingbooth.representative.api.service.RepresentativeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/representatives")
public class RepresentativeController {

    private final RepresentativeService representativeService;

    public RepresentativeController(RepresentativeService representativeService) {
        this.representativeService = representativeService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepresentativeResponse> getRepresentative(@PathVariable UUID id) {
        return ResponseEntity.ok(representativeService.getRepresentative(id));
    }

}
