package com.votingbooth.representative.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "constituency_area")
public class ConstituencyArea {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "chamber_id", nullable = false)
    private UUID chamberId;

    @Column(name = "department_code", nullable = false)
    private String departmentCode;

    @Column(name = "constituency_number", nullable = false)
    private Integer constituencyNumber;

    @Column(name = "commune_code", nullable = false)
    private String communeCode;

    @Column(name = "postcode", nullable = true)
    private String postcode;

    @Column(name = "partial", nullable = false)
    private Boolean partial;

    protected ConstituencyArea() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getChamberId() {
        return chamberId;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public Integer getConstituencyNumber() {
        return constituencyNumber;
    }

    public String getCommuneCode() {
        return communeCode;
    }

    public String getPostcode() {
        return postcode;
    }

    public Boolean getPartial() {
        return partial;
    }
}
