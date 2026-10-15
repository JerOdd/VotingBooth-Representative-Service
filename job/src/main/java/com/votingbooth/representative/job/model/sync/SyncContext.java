package com.votingbooth.representative.job.model.sync;

import java.util.Set;
import java.util.UUID;

// What every record of a run needs. legislatures holds the synced terms' numbers: records of any other legislature are skipped.
public record SyncContext(
        UUID sourceId,
        String sourceCode,
        UUID jurisdictionId,
        UUID chamberId,
        UUID runId,
        Set<Integer> legislatures,
        boolean force
) {

    public SyncContext withLegislatures(Set<Integer> legislatures) {
        return new SyncContext(sourceId, sourceCode, jurisdictionId, chamberId, runId, legislatures, force);
    }

    public boolean isSynced(Integer legislature) {
        return legislature != null && legislatures.contains(legislature);
    }
}
