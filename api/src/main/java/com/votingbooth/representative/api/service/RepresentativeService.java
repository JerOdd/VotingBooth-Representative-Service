package com.votingbooth.representative.api.service;

import com.votingbooth.representative.api.model.dto.RepresentativeResponse;
import com.votingbooth.representative.api.model.dto.RepresentativeSummary;
import com.votingbooth.representative.api.model.dto.RepresentativesResponse;
import com.votingbooth.representative.api.model.exception.RepresentativeNotFoundException;
import com.votingbooth.representative.core.dataaccess.repository.MandateRepository;
import com.votingbooth.representative.core.dataaccess.repository.PersonRepository;
import com.votingbooth.representative.core.model.entity.Person;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RepresentativeService {

    // France's Assemblée nationale is the only chamber for now.
    private static final String COUNTRY = "FR";
    private static final String CHAMBER = "AN";

    private final PersonRepository personRepository;
    private final MandateRepository mandateRepository;

    public RepresentativeService(PersonRepository personRepository, MandateRepository mandateRepository) {
        this.personRepository = personRepository;
        this.mandateRepository = mandateRepository;
    }

    public RepresentativesResponse getRepresentatives() {
        return new RepresentativesResponse(mandateRepository.findCurrentSeats(COUNTRY, CHAMBER).stream()
                .map(seat -> new RepresentativeSummary(
                        seat.getPersonId(),
                        seat.getCivility(),
                        seat.getFirstName(),
                        seat.getLastName(),
                        seat.getGroupName(),
                        seat.getGroupShortName(),
                        seat.getGroupColor(),
                        seat.getDepartmentCode(),
                        seat.getDepartmentName(),
                        seat.getConstituencyNumber()
                ))
                .toList());
    }

    public RepresentativeResponse getRepresentative(UUID id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RepresentativeNotFoundException(id));
        return new RepresentativeResponse(
                person.getId(),
                person.getCivility(),
                person.getFirstName(),
                person.getLastName()
        );
    }

}
