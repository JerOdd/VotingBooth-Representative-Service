package com.votingbooth.representative.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "constituency")
public class Constituency {

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

    @Column(name = "region", nullable = true)
    private String region;

    @Column(name = "department_code", nullable = true)
    private String departmentCode;

    @Column(name = "department_name", nullable = true)
    private String departmentName;

    @Column(name = "number", nullable = true)
    private Integer number;

    @Column(name = "valid_from", nullable = true)
    private LocalDate validFrom;

    @Column(name = "valid_to", nullable = true)
    private LocalDate validTo;

    protected Constituency() {
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

    public String getRegion() {
        return region;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public Integer getNumber() {
        return number;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }
}
