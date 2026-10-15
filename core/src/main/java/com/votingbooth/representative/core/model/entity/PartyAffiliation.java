package com.votingbooth.representative.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "party_affiliation")
public class PartyAffiliation {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = true)
    private LocalDate endDate;

    protected PartyAffiliation() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public String getExternalId() {
        return externalId;
    }

    public UUID getPersonId() {
        return personId;
    }

    public UUID getPartyId() {
        return partyId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
