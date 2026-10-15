package com.votingbooth.representative.job.model.sync;

import com.votingbooth.representative.job.model.amo.AmoEntry;
import com.votingbooth.representative.job.model.amo.Organe;

// An organe whose file changed since the last run, held until its pass: legislatures are synced before every other organe.
public record ChangedOrgane(AmoEntry entry, String sha256, Organe organe) {
}
