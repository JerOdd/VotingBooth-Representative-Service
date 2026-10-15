package com.votingbooth.representative.api.service;

import com.votingbooth.representative.api.dataaccess.repository.RepresentativeRepository;
import com.votingbooth.representative.api.model.dto.RepresentativeResponse;
import com.votingbooth.representative.api.model.entity.Representative;
import com.votingbooth.representative.api.model.exception.RepresentativeNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RepresentativeService {

    private final RepresentativeRepository representativeRepository;

    public RepresentativeService(RepresentativeRepository representativeRepository) {
        this.representativeRepository = representativeRepository;
    }

    public RepresentativeResponse getRepresentative(UUID id) {
        Representative representative = representativeRepository.findById(id)
                .orElseThrow(() -> new RepresentativeNotFoundException(id));
        return new RepresentativeResponse(
                representative.getId(),
                representative.getFirstName(),
                representative.getLastName(),
                representative.getPartyId()
        );
    }

}
