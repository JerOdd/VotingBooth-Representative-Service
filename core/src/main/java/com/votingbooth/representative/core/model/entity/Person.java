package com.votingbooth.representative.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "person")
public class Person {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "civility", nullable = true)
    private String civility;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "sort_name", nullable = false)
    private String sortName;

    @Column(name = "birth_date", nullable = true)
    private LocalDate birthDate;

    @Column(name = "birth_place", nullable = true)
    private String birthPlace;

    @Column(name = "death_date", nullable = true)
    private LocalDate deathDate;

    @Column(name = "profession", nullable = true)
    private String profession;

    @Column(name = "profession_category", nullable = true)
    private String professionCategory;

    @Column(name = "transparency_url", nullable = true)
    private String transparencyUrl;

    @Column(name = "photo_url", nullable = true)
    private String photoUrl;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Person() {
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

    public String getCivility() {
        return civility;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getSortName() {
        return sortName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public LocalDate getDeathDate() {
        return deathDate;
    }

    public String getProfession() {
        return profession;
    }

    public String getProfessionCategory() {
        return professionCategory;
    }

    public String getTransparencyUrl() {
        return transparencyUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
