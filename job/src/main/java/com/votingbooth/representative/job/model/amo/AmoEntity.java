package com.votingbooth.representative.job.model.amo;

// The kinds of file in an AMO archive. The name is the archive directory, and is reused for raw_record.entity and S3 keys.
public enum AmoEntity {
    ACTEUR("acteur"),
    ORGANE("organe"),
    DEPORT("deport");

    private final String directory;

    AmoEntity(String directory) {
        this.directory = directory;
    }

    public String directory() {
        return directory;
    }
}
