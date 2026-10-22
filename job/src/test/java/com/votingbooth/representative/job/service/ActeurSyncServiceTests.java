package com.votingbooth.representative.job.service;

import com.votingbooth.representative.core.dataaccess.repository.GroupMembershipRepository;
import com.votingbooth.representative.core.dataaccess.repository.MandateRepository;
import com.votingbooth.representative.core.dataaccess.repository.PartyAffiliationRepository;
import com.votingbooth.representative.core.dataaccess.repository.PersonContactRepository;
import com.votingbooth.representative.core.dataaccess.repository.PersonRepository;
import com.votingbooth.representative.job.model.amo.Acteur;
import com.votingbooth.representative.job.model.sync.SyncContext;
import com.votingbooth.representative.job.utils.AmoFixtures;
import com.votingbooth.representative.job.utils.AmoParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ActeurSyncServiceTests {

    private static final UUID SOURCE_ID = UUID.randomUUID();
    private static final SyncContext CONTEXT = new SyncContext(SOURCE_ID, "assemblee-nationale", UUID.randomUUID(),
            UUID.randomUUID(), UUID.randomUUID(), Set.of(17), false);
    private static final Instant NOW = Instant.parse("2026-10-15T10:00:00Z");

    private final AmoParser parser = new AmoParser();
    private PersonRepository personRepository;
    private PersonContactRepository personContactRepository;
    private MandateRepository mandateRepository;
    private GroupMembershipRepository groupMembershipRepository;
    private PartyAffiliationRepository partyAffiliationRepository;
    private ActeurSyncService acteurSyncService;

    @BeforeEach
    void setUp() {
        personRepository = mock(PersonRepository.class);
        personContactRepository = mock(PersonContactRepository.class);
        mandateRepository = mock(MandateRepository.class);
        groupMembershipRepository = mock(GroupMembershipRepository.class);
        partyAffiliationRepository = mock(PartyAffiliationRepository.class);
        acteurSyncService = new ActeurSyncService(personRepository, personContactRepository, mandateRepository,
                groupMembershipRepository, partyAffiliationRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void person() {
        acteurSyncService.sync(CONTEXT, acteur("PA794750"));

        verify(personRepository).upsert(SOURCE_ID, "PA794750", "M.", "Thierry", "Liger", "Liger",
                LocalDate.of(1970, 5, 27), "Nogent-Le-Rotrou", null, "Chef d'entreprise",
                "Chefs d'entreprise de 10 salariés ou plus",
                "https://www.hatvp.fr/pages_nominatives/liger-thierry-22478",
                "https://www.assemblee-nationale.fr/dyn/static/tribun/17/photos/carre/794750.jpg", LocalDateTime.of(2026, 10, 15, 10, 0));
    }

    @Test
    void contactsAreReplaced() {
        acteurSyncService.sync(CONTEXT, acteur("PA794750"));

        var inOrder = inOrder(personContactRepository);
        inOrder.verify(personContactRepository).deleteForPerson(SOURCE_ID, "PA794750");
        inOrder.verify(personContactRepository).insert(SOURCE_ID, "PA794750", "OFFICE_ADDRESS",
                "Assemblée nationale\n126 Rue de l'Université\n75355 Paris 07 SP", 0);
        inOrder.verify(personContactRepository).insert(SOURCE_ID, "PA794750", "EMAIL",
                "thierry.liger@assemblee-nationale.fr", 1);
    }

    @Test
    void contactMapping() {
        Acteur acteur = acteur("PA1", List.of(
                electronic("Télécopie", "01 40 63 93 75"),
                electronic("Url sénateur", "https://www.senat.fr/senateur/x.html"),
                electronic("Contact presse", null),
                electronic("Pigeon voyageur", "rooftop"),
                electronic("Twitter", "@jane"),
                electronic("Linkedin", "/in/jane")
        ), List.of());

        acteurSyncService.sync(CONTEXT, acteur);

        verify(personContactRepository).insert(SOURCE_ID, "PA1", "X", "@jane", 0);
        verify(personContactRepository).insert(SOURCE_ID, "PA1", "LINKEDIN", "/in/jane", 1);
        verify(personContactRepository, times(2)).insert(any(), any(), any(), any(), any());
    }

    @Test
    void seatReplacingAnotherMandate() {
        acteurSyncService.sync(CONTEXT, acteur("PA794750"));

        verify(mandateRepository).upsert(eq(SOURCE_ID), eq("PM881146"), eq("PA794750"), eq("PO838901"), eq("PO839629"),
                eq(LocalDate.of(2024, 7, 7)), isNull(), startsWith("remplacement d'un député ayant démissionné"),
                isNull(), eq("PM858579"), eq(196), eq(false));
        verify(mandateRepository, times(2)).upsert(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any());
    }

    @Test
    void groupMemberships() {
        acteurSyncService.sync(CONTEXT, acteur("PA794750"));

        verify(groupMembershipRepository).upsert(SOURCE_ID, "PM858616", "PA794750", "PO840056", "MEMBER",
                LocalDate.of(2025, 1, 24), LocalDate.of(2025, 1, 24));
        verify(groupMembershipRepository).upsert(SOURCE_ID, "PM881147", "PA794750", "PO845425", "MEMBER",
                LocalDate.of(2026, 4, 22), null);
        verify(groupMembershipRepository, times(3)).upsert(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void mandatesOfUnsyncedLegislaturesAreSkipped() {
        acteurSyncService.sync(CONTEXT, acteur("PA720888"));

        verify(personRepository).upsert(eq(SOURCE_ID), eq("PA720888"), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
        verifyNoInteractions(mandateRepository);
    }

    @Test
    void noPhotoOutsideCurrentLegislature() {
        acteurSyncService.sync(CONTEXT, acteur("PA720888"));

        verify(personRepository).upsert(eq(SOURCE_ID), eq("PA720888"), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), isNull(), any());
    }

    @Test
    void partyAffiliation() {
        Acteur acteur = acteur("PA1", List.of(), List.of(
                mandat("PM1", "PARPOL", null, "PO761239", null)
        ));

        acteurSyncService.sync(CONTEXT, acteur);

        verify(partyAffiliationRepository).upsert(SOURCE_ID, "PM1", "PA1", "PO761239", LocalDate.of(2024, 7, 1), null);
    }

    @Test
    void unknownGroupRole() {
        Acteur acteur = acteur("PA1", List.of(), List.of(
                mandat("PM1", "GP", 17, "PO845401", "Trésorier")
        ));

        assertThrows(IllegalArgumentException.class, () -> acteurSyncService.sync(CONTEXT, acteur));
    }

    private Acteur acteur(String uid) {
        return parser.parseActeur(AmoFixtures.bytes("acteur/" + uid + ".json"));
    }

    private static Acteur acteur(String uid, List<Acteur.Adresse> adresses, List<Acteur.Mandat> mandats) {
        return new Acteur(uid,
                new Acteur.EtatCivil(new Acteur.Ident("Mme", "Jane", "Doe", "Doe"), null, null),
                null,
                null,
                new Acteur.Adresses(adresses),
                new Acteur.Mandats(mandats));
    }

    private static Acteur.Adresse electronic(String typeLibelle, String value) {
        return new Acteur.Adresse(null, typeLibelle, null, null, null, null, null, null, null, value);
    }

    private static Acteur.Mandat mandat(String uid, String typeOrgane, Integer legislature, String organeRef,
                                        String codeQualite) {
        return new Acteur.Mandat(uid, typeOrgane, legislature, LocalDate.of(2024, 7, 1), null,
                new Acteur.InfosQualite(codeQualite), new Acteur.Organes(List.of(organeRef)), null, null);
    }

}
