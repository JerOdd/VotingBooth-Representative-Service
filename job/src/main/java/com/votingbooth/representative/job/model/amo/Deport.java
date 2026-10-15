package com.votingbooth.representative.job.model.amo;

import java.time.OffsetDateTime;

// An AMO "deport" file: a recusal declared by a deputy. explication is HTML-escaped text. Names follow the source's JSON.
public record Deport(
        String uid,
        Integer legislature,
        String refActeur,
        OffsetDateTime dateCreation,
        OffsetDateTime datePublication,
        CodeLibelle portee,
        CodeLibelle lecture,
        CodeLibelle instance,
        Cible cible,
        String explication
) {

    public record CodeLibelle(String code, String libelle) {
    }

    public record Cible(CodeLibelle type, String referenceTextuelle) {
    }
}
