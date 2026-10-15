package com.votingbooth.representative.core.config;

import com.votingbooth.representative.core.dataaccess.repository.PersonRepository;
import com.votingbooth.representative.core.model.entity.Person;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// The applications live in com.votingbooth.representative.api/job, so Spring Boot's default scanning doesn't reach representative-core.
@Configuration
@EntityScan(basePackageClasses = Person.class)
@EnableJpaRepositories(basePackageClasses = PersonRepository.class)
public class RepresentativeCoreConfig {
}
