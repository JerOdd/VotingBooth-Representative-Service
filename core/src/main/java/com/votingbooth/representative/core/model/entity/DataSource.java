package com.votingbooth.representative.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "data_source")
public class DataSource {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "jurisdiction_id", nullable = false)
    private UUID jurisdictionId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "url", nullable = true)
    private String url;

    @Column(name = "licence", nullable = true)
    private String licence;

    protected DataSource() {
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

    public String getUrl() {
        return url;
    }

    public String getLicence() {
        return licence;
    }
}
