package com.votingbooth.representative.job.service;

import com.votingbooth.representative.core.dataaccess.repository.RecusalRepository;
import com.votingbooth.representative.job.model.amo.Deport;
import com.votingbooth.representative.job.model.sync.SyncContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class DeportSyncService {

    private final RecusalRepository recusalRepository;

    public DeportSyncService(RecusalRepository recusalRepository) {
        this.recusalRepository = recusalRepository;
    }

    // Some recusals have no publication date yet: the creation date stands in for it.
    @Transactional
    public void sync(SyncContext context, Deport deport) {
        if (!context.isSynced(deport.legislature())) {
            return;
        }
        OffsetDateTime published = deport.datePublication() != null ? deport.datePublication() : deport.dateCreation();
        recusalRepository.upsert(
                context.sourceId(),
                deport.uid(),
                deport.refActeur(),
                context.chamberId(),
                deport.legislature(),
                libelle(deport.portee()),
                libelle(deport.instance()),
                libelle(deport.lecture()),
                deport.cible() == null ? null : libelle(deport.cible().type()),
                deport.cible() == null ? null : deport.cible().referenceTextuelle(),
                deport.explication(),
                published == null ? null : published.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime()
        );
    }

    private static String libelle(Deport.CodeLibelle codeLibelle) {
        return codeLibelle == null ? null : codeLibelle.libelle();
    }

}
