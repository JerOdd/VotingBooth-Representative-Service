package com.votingbooth.representative.job.model.amo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

// An AMO "acteur" file: one person with all their mandates. Names follow the source's JSON.
public record Acteur(
        String uid,
        EtatCivil etatCivil,
        Profession profession,
        @JsonProperty("uri_hatvp") String uriHatvp,
        Adresses adresses,
        Mandats mandats
) {

    public List<Adresse> adresseList() {
        return adresses == null || adresses.adresse() == null ? List.of() : adresses.adresse();
    }

    public List<Mandat> mandatList() {
        return mandats == null || mandats.mandat() == null ? List.of() : mandats.mandat();
    }

    public record EtatCivil(Ident ident, InfoNaissance infoNaissance, LocalDate dateDeces) {
    }

    public record Ident(String civ, String prenom, String nom, String alpha) {
    }

    public record InfoNaissance(LocalDate dateNais, String villeNais, String depNais, String paysNais) {
    }

    public record Profession(String libelleCourant, SocProcInsee socProcINSEE) {
    }

    public record SocProcInsee(String catSocPro, String famSocPro) {
    }

    public record Adresses(List<Adresse> adresse) {
    }

    // Postal addresses use the street fields, electronic ones (email, phone, social networks) use valElec.
    public record Adresse(
            String uid,
            String typeLibelle,
            Integer poids,
            String intitule,
            String numeroRue,
            String nomRue,
            String complementAdresse,
            String codePostal,
            String ville,
            String valElec
    ) {
    }

    public record Mandats(List<Mandat> mandat) {
    }

    // typeOrgane tells mandates apart: ASSEMBLEE is a seat, GP a group membership, PARPOL a party affiliation.
    public record Mandat(
            String uid,
            String typeOrgane,
            Integer legislature,
            LocalDate dateDebut,
            LocalDate dateFin,
            InfosQualite infosQualite,
            Organes organes,
            Election election,
            Mandature mandature
    ) {

        public String organeRef() {
            return organes == null || organes.organeRef() == null || organes.organeRef().isEmpty()
                    ? null
                    : organes.organeRef().getFirst();
        }
    }

    public record InfosQualite(String codeQualite) {
    }

    public record Organes(List<String> organeRef) {
    }

    public record Election(Lieu lieu, String causeMandat, String refCirconscription) {
    }

    public record Lieu(String region, String regionType, String departement, String numDepartement, Integer numCirco) {
    }

    // premiereElection is "1" or "0" in the source.
    public record Mandature(
            LocalDate datePriseFonction,
            String causeFin,
            String premiereElection,
            Integer placeHemicycle,
            String mandatRemplaceRef
    ) {
    }
}
