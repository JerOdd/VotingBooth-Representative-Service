package com.votingbooth.representative.job.model.amo;

// One file of an AMO archive, kept as raw bytes so it can be hashed and stored before (or without) being parsed.
public record AmoEntry(AmoEntity entity, String externalId, byte[] content) {
}
