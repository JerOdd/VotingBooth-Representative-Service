package com.votingbooth.representative.job.service;

import com.votingbooth.representative.core.dataaccess.repository.GroupMembershipRepository;
import com.votingbooth.representative.core.dataaccess.repository.MandateRepository;
import com.votingbooth.representative.core.dataaccess.repository.PartyAffiliationRepository;
import com.votingbooth.representative.core.dataaccess.repository.PersonContactRepository;
import com.votingbooth.representative.core.dataaccess.repository.PersonRepository;
import com.votingbooth.representative.core.model.entity.ContactType;
import com.votingbooth.representative.core.model.entity.GroupRole;
import com.votingbooth.representative.job.model.amo.Acteur;
import com.votingbooth.representative.job.model.sync.SyncContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Map.entry;

@Service
public class ActeurSyncService {

    private static final String SEAT = "ASSEMBLEE";
    private static final String GROUP = "GP";
    private static final String PARTY = "PARPOL";

    // The Assemblée only publishes photos of the current legislature's deputies, under the uid without its "PA".
    private static final int PHOTO_LEGISLATURE = 17;
    private static final String PHOTO_URL = "https://www.assemblee-nationale.fr/dyn/static/tribun/" + PHOTO_LEGISLATURE
            + "/photos/carre/%s.jpg";

    // "Url sénateur" (the Sénat is out of scope) and "Télécopie" (fax) are left out on purpose.
    private static final Map<String, ContactType> CONTACT_TYPES = Map.ofEntries(
            entry("Mèl", ContactType.EMAIL),
            entry("Contact presse", ContactType.EMAIL),
            entry("Téléphone", ContactType.PHONE),
            entry("Adresse officielle", ContactType.OFFICE_ADDRESS),
            entry("Adresse publiée pour Paris ou sa région", ContactType.OFFICE_ADDRESS),
            entry("Adresse publiée de circonscription", ContactType.CONSTITUENCY_ADDRESS),
            entry("Twitter", ContactType.X),
            entry("Facebook", ContactType.FACEBOOK),
            entry("Instagram", ContactType.INSTAGRAM),
            entry("Linkedin", ContactType.LINKEDIN),
            entry("Site internet", ContactType.WEBSITE)
    );

    // A non-inscrit is a member of the NI group.
    private static final Map<String, GroupRole> GROUP_ROLES = Map.of(
            "Membre", GroupRole.MEMBER,
            "Membre apparenté", GroupRole.AFFILIATED,
            "Président", GroupRole.PRESIDENT,
            "Député non-inscrit", GroupRole.MEMBER
    );

    private final PersonRepository personRepository;
    private final PersonContactRepository personContactRepository;
    private final MandateRepository mandateRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final PartyAffiliationRepository partyAffiliationRepository;
    private final Clock clock;

    public ActeurSyncService(PersonRepository personRepository, PersonContactRepository personContactRepository,
                             MandateRepository mandateRepository, GroupMembershipRepository groupMembershipRepository,
                             PartyAffiliationRepository partyAffiliationRepository, Clock clock) {
        this.personRepository = personRepository;
        this.personContactRepository = personContactRepository;
        this.mandateRepository = mandateRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.partyAffiliationRepository = partyAffiliationRepository;
        this.clock = clock;
    }

    // Every person is kept (senators and ministers can author bills), but only Assemblée nationale seats become mandates.
    @Transactional
    public void sync(SyncContext context, Acteur acteur) {
        syncPerson(context, acteur);
        syncContacts(context, acteur);
        for (Acteur.Mandat mandat : acteur.mandatList()) {
            switch (mandat.typeOrgane()) {
                case SEAT -> {
                    if (context.isSynced(mandat.legislature())) {
                        syncSeat(context, acteur, mandat);
                    }
                }
                case GROUP -> {
                    if (context.isSynced(mandat.legislature())) {
                        syncGroupMembership(context, acteur, mandat);
                    }
                }
                case PARTY -> partyAffiliationRepository.upsert(context.sourceId(), mandat.uid(), acteur.uid(),
                        mandat.organeRef(), mandat.dateDebut(), mandat.dateFin());
                default -> {
                }
            }
        }
    }

    private void syncPerson(SyncContext context, Acteur acteur) {
        Acteur.EtatCivil etatCivil = acteur.etatCivil();
        Acteur.Ident ident = etatCivil.ident();
        Acteur.InfoNaissance naissance = etatCivil.infoNaissance();
        Acteur.Profession profession = acteur.profession();
        personRepository.upsert(
                context.sourceId(),
                acteur.uid(),
                ident.civ(),
                ident.prenom(),
                ident.nom(),
                ident.alpha() != null ? ident.alpha() : ident.nom(),
                naissance == null ? null : naissance.dateNais(),
                naissance == null ? null : naissance.villeNais(),
                etatCivil.dateDeces(),
                profession == null ? null : profession.libelleCourant(),
                profession == null || profession.socProcINSEE() == null ? null : profession.socProcINSEE().catSocPro(),
                acteur.uriHatvp(),
                photoUrl(acteur),
                LocalDateTime.now(clock)
        );
    }

    // Anyone else would get a broken link, so they have no photo.
    static String photoUrl(Acteur acteur) {
        boolean currentDeputy = acteur.mandatList().stream()
                .anyMatch(mandat -> SEAT.equals(mandat.typeOrgane())
                        && Integer.valueOf(PHOTO_LEGISLATURE).equals(mandat.legislature()));
        return currentDeputy ? PHOTO_URL.formatted(acteur.uid().replaceFirst("^PA", "")) : null;
    }

    private void syncContacts(SyncContext context, Acteur acteur) {
        personContactRepository.deleteForPerson(context.sourceId(), acteur.uid());
        int position = 0;
        for (Acteur.Adresse adresse : acteur.adresseList()) {
            ContactType type = CONTACT_TYPES.get(adresse.typeLibelle());
            String value = contactValue(adresse);
            if (type != null && !value.isEmpty()) {
                personContactRepository.insert(context.sourceId(), acteur.uid(), type.name(), value, position++);
            }
        }
    }

    // Electronic contacts are in valElec; postal ones are rebuilt one line per part, without the source's trailing commas.
    static String contactValue(Acteur.Adresse adresse) {
        if (adresse.valElec() != null && !adresse.valElec().isBlank()) {
            return adresse.valElec().strip();
        }
        List<String> lines = new ArrayList<>();
        lines.add(adresse.intitule());
        lines.add(join(adresse.numeroRue(), adresse.nomRue()));
        lines.add(adresse.complementAdresse());
        lines.add(join(adresse.codePostal(), adresse.ville()));
        return lines.stream()
                .filter(Objects::nonNull)
                .map(line -> line.strip().replaceAll(",+$", "").strip())
                .filter(line -> !line.isEmpty())
                .collect(Collectors.joining("\n"));
    }

    private static String join(String first, String second) {
        String joined = Stream.of(first, second)
                .filter(Objects::nonNull)
                .map(part -> part.strip().replaceAll(",+$", "").strip())
                .filter(part -> !part.isEmpty())
                .collect(Collectors.joining(" "));
        return joined.isEmpty() ? null : joined;
    }

    private void syncSeat(SyncContext context, Acteur acteur, Acteur.Mandat mandat) {
        Acteur.Election election = mandat.election();
        Acteur.Mandature mandature = mandat.mandature();
        mandateRepository.upsert(
                context.sourceId(),
                mandat.uid(),
                acteur.uid(),
                mandat.organeRef(),
                election == null ? null : election.refCirconscription(),
                mandat.dateDebut(),
                mandat.dateFin(),
                election == null ? null : election.causeMandat(),
                mandature == null ? null : mandature.causeFin(),
                mandature == null ? null : mandature.mandatRemplaceRef(),
                mandature == null ? null : mandature.placeHemicycle(),
                mandature == null || mandature.premiereElection() == null ? null : "1".equals(mandature.premiereElection())
        );
    }

    private void syncGroupMembership(SyncContext context, Acteur acteur, Acteur.Mandat mandat) {
        String codeQualite = mandat.infosQualite() == null ? null : mandat.infosQualite().codeQualite();
        GroupRole role = GROUP_ROLES.get(codeQualite);
        if (role == null) {
            throw new IllegalArgumentException("Unknown group role '" + codeQualite + "' on mandate " + mandat.uid());
        }
        groupMembershipRepository.upsert(context.sourceId(), mandat.uid(), acteur.uid(), mandat.organeRef(),
                role.name(), mandat.dateDebut(), mandat.dateFin());
    }

}
