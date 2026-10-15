package com.votingbooth.representative.job.service;

import com.votingbooth.representative.core.dataaccess.repository.ConstituencyRepository;
import com.votingbooth.representative.core.dataaccess.repository.PartyRepository;
import com.votingbooth.representative.core.dataaccess.repository.PoliticalGroupRepository;
import com.votingbooth.representative.core.dataaccess.repository.TermRepository;
import com.votingbooth.representative.job.model.amo.Organe;
import com.votingbooth.representative.job.model.sync.SyncContext;
import com.votingbooth.representative.job.utils.AmoFixtures;
import com.votingbooth.representative.job.utils.AmoParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class OrganeSyncServiceTests {

    private static final UUID SOURCE_ID = UUID.randomUUID();
    private static final UUID JURISDICTION_ID = UUID.randomUUID();
    private static final UUID CHAMBER_ID = UUID.randomUUID();
    private static final SyncContext CONTEXT = new SyncContext(SOURCE_ID, "assemblee-nationale", JURISDICTION_ID,
            CHAMBER_ID, UUID.randomUUID(), Set.of(17), false);

    private final AmoParser parser = new AmoParser();
    private TermRepository termRepository;
    private PoliticalGroupRepository politicalGroupRepository;
    private PartyRepository partyRepository;
    private ConstituencyRepository constituencyRepository;
    private OrganeSyncService organeSyncService;

    @BeforeEach
    void setUp() {
        termRepository = mock(TermRepository.class);
        politicalGroupRepository = mock(PoliticalGroupRepository.class);
        partyRepository = mock(PartyRepository.class);
        constituencyRepository = mock(ConstituencyRepository.class);
        organeSyncService = new OrganeSyncService(termRepository, politicalGroupRepository, partyRepository,
                constituencyRepository);
    }

    @Test
    void legislature() {
        organeSyncService.sync(CONTEXT, organe("PO838901"));

        verify(termRepository).upsert(SOURCE_ID, "PO838901", CHAMBER_ID, 17, LocalDate.of(2024, 7, 1), null);
    }

    @Test
    void politicalGroup() {
        organeSyncService.sync(CONTEXT, organe("PO845401"));

        verify(politicalGroupRepository).upsert(SOURCE_ID, "PO845401", CHAMBER_ID, 17, "Rassemblement National", "RN",
                "#313567", LocalDate.of(2024, 7, 18), null);
    }

    @Test
    void groupOfUnsyncedLegislature() {
        organeSyncService.sync(CONTEXT.withLegislatures(Set.of(16)), organe("PO845401"));

        verifyNoInteractions(politicalGroupRepository);
    }

    @Test
    void constituency() {
        organeSyncService.sync(CONTEXT, organe("PO839063"));

        verify(constituencyRepository).upsert(SOURCE_ID, "PO839063", CHAMBER_ID, 17, "5ème circonscription de l'Ain",
                "Auvergne-Rhône-Alpes", "01", "Ain", 5, LocalDate.of(2024, 7, 1), null);
    }

    @Test
    void constituencyOfUnsyncedLegislature() {
        organeSyncService.sync(CONTEXT.withLegislatures(Set.of(16)), organe("PO839063"));

        verifyNoInteractions(constituencyRepository);
    }

    @Test
    void party() {
        organeSyncService.sync(CONTEXT, organe("PO761239"));

        verify(partyRepository).upsert(SOURCE_ID, "PO761239", JURISDICTION_ID, "Rassemblement national", "RN");
    }

    @Test
    void otherOrganeTypesAreIgnored() {
        Organe committee = new Organe("PO1", "COMPER", "Commission des lois", null, "LOIS",
                new Organe.ViMoDe(LocalDate.of(2024, 7, 1), null), 17, null, null, null);

        organeSyncService.sync(CONTEXT, committee);

        verifyNoInteractions(termRepository, politicalGroupRepository, partyRepository, constituencyRepository);
    }

    private Organe organe(String uid) {
        return parser.parseOrgane(AmoFixtures.bytes("organe/" + uid + ".json"));
    }

}
