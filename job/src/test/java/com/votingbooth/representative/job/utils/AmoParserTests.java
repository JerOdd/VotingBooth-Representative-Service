package com.votingbooth.representative.job.utils;

import com.votingbooth.representative.job.model.amo.Acteur;
import com.votingbooth.representative.job.model.amo.Deport;
import com.votingbooth.representative.job.model.amo.Organe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Fixtures are real files from the AMO30 archive, each picked for one quirk of the XML→JSON conversion.
public class AmoParserTests {

    private AmoParser parser;

    @BeforeEach
    void setUp() {
        parser = new AmoParser();
    }

    @Test
    void acteur() {
        Acteur acteur = parser.parseActeur(AmoFixtures.bytes("acteur/PA794750.json"));

        assertEquals("PA794750", acteur.uid());
        assertEquals("M.", acteur.etatCivil().ident().civ());
        assertEquals("Thierry", acteur.etatCivil().ident().prenom());
        assertEquals("Liger", acteur.etatCivil().ident().alpha());
        assertEquals(LocalDate.of(1970, 5, 27), acteur.etatCivil().infoNaissance().dateNais());
        assertNull(acteur.etatCivil().dateDeces());
        assertEquals("Chef d'entreprise", acteur.profession().libelleCourant());
        assertEquals("https://www.hatvp.fr/pages_nominatives/liger-thierry-22478", acteur.uriHatvp());
        assertEquals(16, acteur.mandatList().size());
        assertEquals(List.of("Adresse officielle", "Mèl"),
                acteur.adresseList().stream().map(Acteur.Adresse::typeLibelle).toList());
    }

    @Test
    void seatReplacingAnotherMandate() {
        Acteur acteur = parser.parseActeur(AmoFixtures.bytes("acteur/PA794750.json"));

        Acteur.Mandat seat = mandate(acteur, "PM881146");

        assertEquals("ASSEMBLEE", seat.typeOrgane());
        assertEquals(17, seat.legislature());
        assertEquals("PO838901", seat.organeRef());
        assertEquals(LocalDate.of(2024, 7, 7), seat.dateDebut());
        assertNull(seat.dateFin());
        assertEquals("PO839629", seat.election().refCirconscription());
        assertEquals("61", seat.election().lieu().numDepartement());
        assertEquals(2, seat.election().lieu().numCirco());
        assertEquals("PM858579", seat.mandature().mandatRemplaceRef());
        assertEquals(196, seat.mandature().placeHemicycle());
        assertEquals("0", seat.mandature().premiereElection());
        assertNull(seat.mandature().causeFin());
    }

    @Test
    void groupMembership() {
        Acteur acteur = parser.parseActeur(AmoFixtures.bytes("acteur/PA794750.json"));

        Acteur.Mandat membership = mandate(acteur, "PM858616");

        assertEquals("GP", membership.typeOrgane());
        assertEquals("Député non-inscrit", membership.infosQualite().codeQualite());
        assertEquals("PO840056", membership.organeRef());
        assertEquals(LocalDate.of(2025, 1, 24), membership.dateFin());
    }

    @Test
    void singleMandateAndAddressAsObjects() {
        Acteur acteur = parser.parseActeur(AmoFixtures.bytes("acteur/PA720888.json"));

        assertEquals(1, acteur.mandatList().size());
        assertEquals("PM772923", acteur.mandatList().getFirst().uid());
        assertNull(acteur.mandatList().getFirst().mandature().placeHemicycle());
        assertEquals(1, acteur.adresseList().size());
        assertEquals("AD720890", acteur.adresseList().getFirst().uid());
    }

    @Test
    void nilAddresses() {
        Acteur acteur = parser.parseActeur(AmoFixtures.bytes("acteur/PA427486.json"));

        assertNull(acteur.adresses());
        assertTrue(acteur.adresseList().isEmpty());
        assertEquals(2, acteur.mandatList().size());
    }

    @Test
    void deceasedWithoutElection() {
        Acteur acteur = parser.parseActeur(AmoFixtures.bytes("acteur/PA497.json"));

        assertEquals(LocalDate.of(2017, 9, 15), acteur.etatCivil().dateDeces());
        Acteur.Mandat mandat = acteur.mandatList().getFirst();
        assertEquals("MINISTERE", mandat.typeOrgane());
        assertNull(mandat.election());
        assertNull(mandat.mandature());
    }

    @Test
    void politicalGroup() {
        Organe organe = parser.parseOrgane(AmoFixtures.bytes("organe/PO845401.json"));

        assertEquals("PO845401", organe.uid());
        assertEquals("GP", organe.codeType());
        assertEquals("Rassemblement National", organe.libelle());
        assertEquals("RN", organe.libelleAbrev());
        assertEquals("#313567", organe.couleurAssociee());
        assertEquals(17, organe.legislature());
        assertEquals(LocalDate.of(2024, 7, 18), organe.viMoDe().dateDebut());
        assertNull(organe.viMoDe().dateFin());
    }

    @Test
    void legislature() {
        Organe organe = parser.parseOrgane(AmoFixtures.bytes("organe/PO838901.json"));

        assertEquals("ASSEMBLEE", organe.codeType());
        assertEquals(17, organe.legislature());
        assertEquals(LocalDate.of(2024, 7, 1), organe.viMoDe().dateDebut());
    }

    @Test
    void constituency() {
        Organe organe = parser.parseOrgane(AmoFixtures.bytes("organe/PO839063.json"));

        assertEquals("CIRCONSCRIPTION", organe.codeType());
        assertEquals("5ème circonscription de l'Ain", organe.libelle());
        assertEquals(5, organe.numero());
        assertEquals("01", organe.lieu().departement().code());
        assertEquals("Ain", organe.lieu().departement().libelle());
        assertEquals("Auvergne-Rhône-Alpes", organe.lieu().region().libelle());
    }

    @Test
    void party() {
        Organe organe = parser.parseOrgane(AmoFixtures.bytes("organe/PO761239.json"));

        assertEquals("PARPOL", organe.codeType());
        assertEquals("Rassemblement national", organe.libelle());
        assertNull(organe.legislature());
        assertNull(organe.viMoDe().dateDebut());
    }

    @Test
    void recusal() {
        Deport deport = parser.parseDeport(AmoFixtures.bytes("deport/DPTR5L17PA793992D0003.json"));

        assertEquals("DPTR5L17PA793992D0003", deport.uid());
        assertEquals(17, deport.legislature());
        assertEquals("PA793992", deport.refActeur());
        assertEquals(OffsetDateTime.of(2025, 12, 9, 12, 57, 0, 851961000, ZoneOffset.UTC), deport.dateCreation());
        assertNull(deport.datePublication());
        assertEquals("DELIB", deport.portee().code());
        assertEquals("Toutes lectures restantes", deport.lecture().libelle());
        assertEquals("SEA_COM", deport.instance().code());
        assertEquals("ART", deport.cible().type().code());
        assertEquals("Article 2 du projet de loi de financement de la sécurité sociale pour 2026",
                deport.cible().referenceTextuelle());
        assertNull(deport.explication());
    }

    @Test
    void wrongRoot() {
        assertThrows(IllegalArgumentException.class, () -> parser.parseOrgane(AmoFixtures.bytes("acteur/PA497.json")));
    }

    private static Acteur.Mandat mandate(Acteur acteur, String uid) {
        return acteur.mandatList().stream().filter(m -> uid.equals(m.uid())).findFirst().orElseThrow();
    }


}
