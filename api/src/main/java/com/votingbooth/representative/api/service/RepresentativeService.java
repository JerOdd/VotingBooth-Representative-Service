package com.votingbooth.representative.api.service;

import com.votingbooth.representative.api.model.dto.RepresentativeResponse;
import com.votingbooth.representative.api.model.exception.RepresentativeNotFoundException;
import com.votingbooth.representative.core.dataaccess.repository.PersonRepository;
import com.votingbooth.representative.core.model.entity.Person;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RepresentativeService {

    private final PersonRepository personRepository;

    public RepresentativeService(PersonRepository personRepository) {
        this.personRepository = personRepository;
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
