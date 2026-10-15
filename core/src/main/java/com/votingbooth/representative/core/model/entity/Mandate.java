package com.votingbooth.representative.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "mandate")
public class Mandate {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Column(name = "term_id", nullable = false)
    private UUID termId;

    @Column(name = "constituency_id", nullable = true)
    private UUID constituencyId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = true)
    private LocalDate endDate;

    @Column(name = "start_reason", nullable = true)
    private String startReason;

    @Column(name = "end_reason", nullable = true)
    private String endReason;

    @Column(name = "replaces_mandate_external_id", nullable = true)
    private String replacesMandateExternalId;

    @Column(name = "replaces_mandate_id", nullable = true)
    private UUID replacesMandateId;

    @Column(name = "seat_number", nullable = true)
    private Integer seatNumber;

    @Column(name = "first_elected", nullable = true)
    private Boolean firstElected;

    protected Mandate() {
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

    public UUID getTermId() {
        return termId;
    }

    public UUID getConstituencyId() {
        return constituencyId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getStartReason() {
        return startReason;
    }

    public String getEndReason() {
        return endReason;
    }

    public String getReplacesMandateExternalId() {
        return replacesMandateExternalId;
    }

    public UUID getReplacesMandateId() {
        return replacesMandateId;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public Boolean getFirstElected() {
        return firstElected;
    }
}
