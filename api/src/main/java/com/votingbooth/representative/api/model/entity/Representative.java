package com.votingbooth.representative.api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "representatives")
public class Representative {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    protected Representative() {
    }

    public Representative(UUID id, String firstName, String lastName, UUID partyId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.partyId = partyId;
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public UUID getPartyId() {
        return partyId;
    }
}
