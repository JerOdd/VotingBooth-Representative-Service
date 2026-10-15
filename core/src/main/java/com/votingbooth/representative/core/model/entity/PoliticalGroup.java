package com.votingbooth.representative.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "political_group")
public class PoliticalGroup {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "term_id", nullable = false)
    private UUID termId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "short_name", nullable = true)
    private String shortName;

    @Column(name = "color", nullable = true)
    private String color;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = true)
    private LocalDate endDate;

    protected PoliticalGroup() {
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

    public UUID getTermId() {
        return termId;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getColor() {
        return color;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
