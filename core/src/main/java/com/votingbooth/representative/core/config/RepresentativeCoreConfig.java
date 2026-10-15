package com.votingbooth.representative.core.config;

import com.votingbooth.representative.core.dataaccess.repository.RepresentativeRepository;
import com.votingbooth.representative.core.model.entity.Representative;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// The applications live in com.votingbooth.representative.api/job, so Spring Boot's default scanning doesn't reach representative-core.
@Configuration
@EntityScan(basePackageClasses = Representative.class)
@EnableJpaRepositories(basePackageClasses = RepresentativeRepository.class)
public class RepresentativeCoreConfig {
}
