package com.votingbooth.representative.job.service;

import com.votingbooth.representative.core.dataaccess.repository.ConstituencyRepository;
import com.votingbooth.representative.core.dataaccess.repository.PartyRepository;
import com.votingbooth.representative.core.dataaccess.repository.PoliticalGroupRepository;
import com.votingbooth.representative.core.dataaccess.repository.TermRepository;
import com.votingbooth.representative.job.model.amo.Organe;
import com.votingbooth.representative.job.model.sync.SyncContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganeSyncService {

    static final String LEGISLATURE = "ASSEMBLEE";
    static final String GROUP = "GP";
    static final String PARTY = "PARPOL";
    static final String CONSTITUENCY = "CIRCONSCRIPTION";

    private final TermRepository termRepository;
    private final PoliticalGroupRepository politicalGroupRepository;
    private final PartyRepository partyRepository;
    private final ConstituencyRepository constituencyRepository;

    public OrganeSyncService(TermRepository termRepository, PoliticalGroupRepository politicalGroupRepository,
                             PartyRepository partyRepository, ConstituencyRepository constituencyRepository) {
        this.termRepository = termRepository;
        this.politicalGroupRepository = politicalGroupRepository;
        this.partyRepository = partyRepository;
        this.constituencyRepository = constituencyRepository;
    }

    public static boolean isLegislature(Organe organe) {
        return LEGISLATURE.equals(organe.codeType());
    }

    // Other organe types (committees, friendship groups, ministries...) are kept as raw files only.
    @Transactional
    public void sync(SyncContext context, Organe organe) {
        switch (organe.codeType()) {
            case LEGISLATURE -> termRepository.upsert(context.sourceId(), organe.uid(), context.chamberId(),
                    organe.legislature(), organe.viMoDe().dateDebut(), organe.viMoDe().dateFin());
            case GROUP -> {
                if (context.isSynced(organe.legislature())) {
                    politicalGroupRepository.upsert(context.sourceId(), organe.uid(), context.chamberId(),
                            organe.legislature(), organe.libelle(), organe.libelleAbrev(), organe.couleurAssociee(),
                            organe.viMoDe().dateDebut(), organe.viMoDe().dateFin());
                }
            }
            case PARTY -> partyRepository.upsert(context.sourceId(), organe.uid(), context.jurisdictionId(),
                    organe.libelle(), organe.libelleAbrev());
            case CONSTITUENCY -> {
                if (context.isSynced(organe.legislature())) {
                    Organe.Lieu lieu = organe.lieu();
                    constituencyRepository.upsert(context.sourceId(), organe.uid(), context.chamberId(),
                            organe.legislature(), organe.libelle(),
                            lieu == null || lieu.region() == null ? null : lieu.region().libelle(),
                            lieu == null || lieu.departement() == null ? null : lieu.departement().code(),
                            lieu == null || lieu.departement() == null ? null : lieu.departement().libelle(),
                            organe.numero(), organe.viMoDe().dateDebut(), organe.viMoDe().dateFin());
                }
            }
            default -> {
            }
        }
    }

}
