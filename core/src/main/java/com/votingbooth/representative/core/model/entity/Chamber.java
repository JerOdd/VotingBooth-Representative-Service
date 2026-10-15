package com.votingbooth.representative.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "chamber")
public class Chamber {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "jurisdiction_id", nullable = false)
    private UUID jurisdictionId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private ChamberKind kind;

    @Enumerated(EnumType.STRING)
    @Column(name = "electoral_system", nullable = false)
    private ElectoralSystem electoralSystem;

    @Column(name = "seats", nullable = false)
    private Integer seats;

    protected Chamber() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getJurisdictionId() {
        return jurisdictionId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public ChamberKind getKind() {
        return kind;
    }

    public ElectoralSystem getElectoralSystem() {
        return electoralSystem;
    }

    public Integer getSeats() {
        return seats;
    }
}
