package com.votingbooth.representative.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "jurisdiction")
public class Jurisdiction {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "iso_code", nullable = false)
    private String isoCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private JurisdictionKind kind;

    @Column(name = "default_language", nullable = false)
    private String defaultLanguage;

    protected Jurisdiction() {
    }

    public UUID getId() {
        return id;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public String getName() {
        return name;
    }

    public JurisdictionKind getKind() {
        return kind;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }
}
