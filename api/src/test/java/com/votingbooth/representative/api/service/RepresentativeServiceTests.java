package com.votingbooth.representative.api.service;

import com.votingbooth.representative.api.model.dto.RepresentativeResponse;
import com.votingbooth.representative.api.model.exception.RepresentativeNotFoundException;
import com.votingbooth.representative.core.dataaccess.repository.RepresentativeRepository;
import com.votingbooth.representative.core.model.entity.Representative;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RepresentativeServiceTests {

    private RepresentativeRepository representativeRepository;
    private RepresentativeService representativeService;

    @BeforeEach
    void setUp() {
        representativeRepository = mock(RepresentativeRepository.class);
        representativeService = new RepresentativeService(representativeRepository);
    }

    @Test
    void getRepresentative() {
        UUID id = UUID.randomUUID();
        UUID partyId = UUID.randomUUID();
        when(representativeRepository.findById(id))
                .thenReturn(Optional.of(new Representative(id, "Jane", "Doe", partyId)));

        RepresentativeResponse response = representativeService.getRepresentative(id);

        assertEquals(new RepresentativeResponse(id, "Jane", "Doe", partyId), response);
    }

    @Test
    void representativeNotFound() {
        UUID id = UUID.randomUUID();
        when(representativeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RepresentativeNotFoundException.class, () -> representativeService.getRepresentative(id));
    }

}
