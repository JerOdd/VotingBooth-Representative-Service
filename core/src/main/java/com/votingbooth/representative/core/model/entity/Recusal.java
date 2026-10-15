package com.votingbooth.representative.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recusal")
public class Recusal {

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

    @Column(name = "scope", nullable = true)
    private String scope;

    @Column(name = "instance", nullable = true)
    private String instance;

    @Column(name = "reading", nullable = true)
    private String reading;

    @Column(name = "target_type", nullable = true)
    private String targetType;

    @Column(name = "target_description", nullable = true)
    private String targetDescription;

    @Column(name = "explanation", nullable = true)
    private String explanation;

    @Column(name = "published_at", nullable = true)
    private LocalDateTime publishedAt;

    protected Recusal() {
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

    public String getScope() {
        return scope;
    }

    public String getInstance() {
        return instance;
    }

    public String getReading() {
        return reading;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetDescription() {
        return targetDescription;
    }

    public String getExplanation() {
        return explanation;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}
