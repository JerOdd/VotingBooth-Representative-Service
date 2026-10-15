package com.votingbooth.representative.job.model.amo;

import java.time.LocalDate;

// An AMO "organe" file: a body such as a legislature (ASSEMBLEE), a political group (GP), a party (PARPOL)
// or a constituency (CIRCONSCRIPTION). Names follow the source's JSON.
public record Organe(
        String uid,
        String codeType,
        String libelle,
        String libelleAbrege,
        String libelleAbrev,
        ViMoDe viMoDe,
        Integer legislature,
        Integer numero,
        Lieu lieu,
        String couleurAssociee
) {

    public record ViMoDe(LocalDate dateDebut, LocalDate dateFin) {
    }

    public record Lieu(Region region, Departement departement) {
    }

    public record Region(String type, String libelle) {
    }

    public record Departement(String codeNatureDep, String code, String libelle) {
    }
}
