package com.votingbooth.representativemodule.api.service;

import com.votingbooth.representativemodule.api.dataaccess.RepresentativeRepository;
import com.votingbooth.representativemodule.api.model.Representative;
import com.votingbooth.representativemodule.api.model.RepresentativeNotFoundException;
import com.votingbooth.representativemodule.api.model.RepresentativeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RepresentativeService {
    @Autowired
    private RepresentativeRepository representativeRepository;

    public RepresentativeResponse getRepresentative(UUID id) {
        Representative representative = representativeRepository.findById(id).orElseThrow(() ->
                new RepresentativeNotFoundException(id)
        );
        return new RepresentativeResponse(representative);
    }
}
