package com.votingbooth.representative.api;

import com.votingbooth.representative.core.config.RepresentativeCoreConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(RepresentativeCoreConfig.class)
public class RepresentativeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepresentativeApiApplication.class, args);
    }

}
