package com.votingbooth.representative.api.service;

import com.votingbooth.representative.api.model.dto.RepresentativeResponse;
import com.votingbooth.representative.api.model.exception.RepresentativeNotFoundException;
import com.votingbooth.representative.core.dataaccess.repository.PersonRepository;
import com.votingbooth.representative.core.model.entity.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RepresentativeServiceTests {

    private PersonRepository personRepository;
    private RepresentativeService representativeService;

    @BeforeEach
    void setUp() {
        personRepository = mock(PersonRepository.class);
        representativeService = new RepresentativeService(personRepository);
    }

    @Test
    void getRepresentative() {
        UUID id = UUID.randomUUID();
        Person person = mock(Person.class);
        when(person.getId()).thenReturn(id);
        when(person.getCivility()).thenReturn("Mme");
        when(person.getFirstName()).thenReturn("Jane");
        when(person.getLastName()).thenReturn("Doe");
        when(personRepository.findById(id)).thenReturn(Optional.of(person));

        RepresentativeResponse response = representativeService.getRepresentative(id);

        assertEquals(new RepresentativeResponse(id, "Mme", "Jane", "Doe"), response);
    }

    @Test
    void representativeNotFound() {
        UUID id = UUID.randomUUID();
        when(personRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RepresentativeNotFoundException.class, () -> representativeService.getRepresentative(id));
    }

}
