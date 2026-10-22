package com.votingbooth.representative.api.service;

import com.votingbooth.representative.api.model.dto.RepresentativeResponse;
import com.votingbooth.representative.api.model.dto.RepresentativeSummary;
import com.votingbooth.representative.api.model.dto.RepresentativesResponse;
import com.votingbooth.representative.api.model.exception.RepresentativeNotFoundException;
import com.votingbooth.representative.core.dataaccess.repository.MandateRepository;
import com.votingbooth.representative.core.dataaccess.repository.PersonRepository;
import com.votingbooth.representative.core.model.entity.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RepresentativeServiceTests {

    private PersonRepository personRepository;
    private MandateRepository mandateRepository;
    private RepresentativeService representativeService;

    @BeforeEach
    void setUp() {
        personRepository = mock(PersonRepository.class);
        mandateRepository = mock(MandateRepository.class);
        representativeService = new RepresentativeService(personRepository, mandateRepository);
    }

    @Test
    void getRepresentatives() {
        UUID id = UUID.randomUUID();
        MandateRepository.CurrentSeat seat = mock(MandateRepository.CurrentSeat.class);
        when(seat.getPersonId()).thenReturn(id);
        when(seat.getCivility()).thenReturn("Mme");
        when(seat.getFirstName()).thenReturn("Audrey");
        when(seat.getLastName()).thenReturn("Abadie-Amiel");
        when(seat.getPhotoUrl()).thenReturn("https://www.assemblee-nationale.fr/dyn/static/tribun/17/photos/carre/793218.jpg");
        when(seat.getGroupName()).thenReturn("Libertés, Indépendants, Outre-mer et Territoires");
        when(seat.getGroupShortName()).thenReturn("LIOT");
        when(seat.getGroupColor()).thenReturn("#FFD96F");
        when(seat.getDepartmentCode()).thenReturn("09");
        when(seat.getDepartmentName()).thenReturn("Ariège");
        when(seat.getConstituencyNumber()).thenReturn(2);
        when(mandateRepository.findCurrentSeats("FR", "AN")).thenReturn(List.of(seat));

        RepresentativesResponse response = representativeService.getRepresentatives();

        assertEquals(List.of(new RepresentativeSummary(id, "Mme", "Audrey", "Abadie-Amiel",
                "https://www.assemblee-nationale.fr/dyn/static/tribun/17/photos/carre/793218.jpg", "Libertés, Indépendants, Outre-mer et Territoires", "LIOT", "#FFD96F", "09", "Ariège", 2)),
                response.representatives());
    }

    @Test
    void noRepresentatives() {
        when(mandateRepository.findCurrentSeats("FR", "AN")).thenReturn(List.of());

        assertTrue(representativeService.getRepresentatives().representatives().isEmpty());
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
