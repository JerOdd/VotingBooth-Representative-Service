package com.votingbooth.representative.job;

import com.votingbooth.representative.core.config.RepresentativeCoreConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(RepresentativeCoreConfig.class)
public class RepresentativeJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepresentativeJobApplication.class, args);
    }

}
