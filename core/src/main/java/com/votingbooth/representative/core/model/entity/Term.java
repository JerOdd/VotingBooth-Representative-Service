package com.votingbooth.representative.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "term")
public class Term {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "chamber_id", nullable = false)
    private UUID chamberId;

    @Column(name = "number", nullable = false)
    private Integer number;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = true)
    private LocalDate endDate;

    protected Term() {
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

    public UUID getChamberId() {
        return chamberId;
    }

    public Integer getNumber() {
        return number;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
