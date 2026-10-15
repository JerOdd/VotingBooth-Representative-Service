package com.votingbooth.representative.job.service;

import com.votingbooth.representative.core.dataaccess.repository.RecusalRepository;
import com.votingbooth.representative.job.model.amo.Deport;
import com.votingbooth.representative.job.model.sync.SyncContext;
import com.votingbooth.representative.job.utils.AmoFixtures;
import com.votingbooth.representative.job.utils.AmoParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class DeportSyncServiceTests {

    private static final UUID SOURCE_ID = UUID.randomUUID();
    private static final UUID CHAMBER_ID = UUID.randomUUID();
    private static final SyncContext CONTEXT = new SyncContext(SOURCE_ID, "assemblee-nationale", UUID.randomUUID(),
            CHAMBER_ID, UUID.randomUUID(), Set.of(17), false);

    private RecusalRepository recusalRepository;
    private DeportSyncService deportSyncService;

    @BeforeEach
    void setUp() {
        recusalRepository = mock(RecusalRepository.class);
        deportSyncService = new DeportSyncService(recusalRepository);
    }

    @Test
    void recusalWithoutPublicationDateUsesCreationDate() {
        deportSyncService.sync(CONTEXT, deport());

        verify(recusalRepository).upsert(SOURCE_ID, "DPTR5L17PA793992D0003", "PA793992", CHAMBER_ID, 17,
                "Être présent sans prendre la parole ni voter", "Séance publique et Commission",
                "Toutes lectures restantes", "Article(s)",
                "Article 2 du projet de loi de financement de la sécurité sociale pour 2026", null,
                LocalDateTime.of(2025, 12, 9, 12, 57, 0, 851961000));
    }

    @Test
    void recusalOfUnsyncedLegislature() {
        deportSyncService.sync(CONTEXT.withLegislatures(Set.of(16)), deport());

        verifyNoInteractions(recusalRepository);
    }

    private static Deport deport() {
        return new AmoParser().parseDeport(AmoFixtures.bytes("deport/DPTR5L17PA793992D0003.json"));
    }

}
