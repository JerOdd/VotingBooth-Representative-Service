package com.votingbooth.representative.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "person_contact")
public class PersonContact {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ContactType type;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "position", nullable = false)
    private Integer position;

    protected PersonContact() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getPersonId() {
        return personId;
    }

    public ContactType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Integer getPosition() {
        return position;
    }
}
