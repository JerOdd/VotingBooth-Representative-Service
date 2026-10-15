package com.votingbooth.representative.job;

import com.votingbooth.representative.core.config.RepresentativeCoreConfig;
import com.votingbooth.representative.core.model.entity.SyncTrigger;
import com.votingbooth.representative.job.service.AmoSyncService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

// Runs one sync and exits: the scheduler (cron) starts the container. A failed run exits with a non-zero code.
//   --manual  records the run as MANUAL instead of SCHEDULED
//   --force   reprocesses every record, ignoring the archive's ETag and the stored hashes (implies --manual)
@SpringBootApplication
@Import(RepresentativeCoreConfig.class)
public class RepresentativeJobApplication implements ApplicationRunner {

    private final AmoSyncService amoSyncService;

    public RepresentativeJobApplication(AmoSyncService amoSyncService) {
        this.amoSyncService = amoSyncService;
    }

    public static void main(String[] args) {
        SpringApplication.run(RepresentativeJobApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean force = args.containsOption("force");
        SyncTrigger trigger = force || args.containsOption("manual") ? SyncTrigger.MANUAL : SyncTrigger.SCHEDULED;
        amoSyncService.sync(trigger, force);
    }

}
