-- Enumerations are VARCHAR + CHECK rather than PostgreSQL enum types: easier to extend and validated by Hibernate without custom types.
-- Every synced table carries source_id + external_id, unique per source: that pair is the cross-service contract.

-- Reference data

CREATE TABLE jurisdiction (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    iso_code VARCHAR(10) NOT NULL,
    name VARCHAR(255) NOT NULL,
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('COUNTRY', 'SUPRANATIONAL', 'REGION')),
    default_language VARCHAR(10) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (iso_code)
);

CREATE TABLE data_source (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    jurisdiction_id UUID NOT NULL REFERENCES jurisdiction (id),
    code VARCHAR(50) NOT NULL,
    url VARCHAR(500),
    licence VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE (code)
);

CREATE TABLE chamber (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    jurisdiction_id UUID NOT NULL REFERENCES jurisdiction (id),
    code VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('LOWER', 'UPPER', 'UNICAMERAL')),
    electoral_system VARCHAR(20) NOT NULL CHECK (electoral_system IN ('CONSTITUENCY', 'LIST', 'MIXED')),
    seats INTEGER NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (jurisdiction_id, code)
);

-- A legislature. Synced from the source (AN has one organe per legislature), so it carries an external id.
CREATE TABLE term (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    source_id UUID NOT NULL REFERENCES data_source (id),
    external_id VARCHAR(50) NOT NULL,
    chamber_id UUID NOT NULL REFERENCES chamber (id),
    number INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE, -- null = current term
    PRIMARY KEY (id),
    UNIQUE (source_id, external_id),
    UNIQUE (chamber_id, number)
);

-- People and contacts

CREATE TABLE person (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    source_id UUID NOT NULL REFERENCES data_source (id),
    external_id VARCHAR(50) NOT NULL,
    civility VARCHAR(20),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    sort_name VARCHAR(255) NOT NULL,
    birth_date DATE,
    birth_place VARCHAR(255),
    death_date DATE,
    profession VARCHAR(255),
    profession_category VARCHAR(255),
    transparency_url VARCHAR(500),
    photo_url VARCHAR(500),
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (source_id, external_id)
);

-- Replaced as a whole when the person changes: the source gives contacts no stable ids.
CREATE TABLE person_contact (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    person_id UUID NOT NULL REFERENCES person (id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL CHECK (type IN ('EMAIL', 'PHONE', 'OFFICE_ADDRESS', 'CONSTITUENCY_ADDRESS',
                                              'X', 'FACEBOOK', 'INSTAGRAM', 'LINKEDIN', 'WEBSITE')),
    value TEXT NOT NULL,
    position INTEGER NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (person_id, position)
);

-- Constituencies

-- AN creates a new organe (PO…) for each constituency in each legislature, so a constituency belongs to a term.
CREATE TABLE constituency (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    source_id UUID NOT NULL REFERENCES data_source (id),
    external_id VARCHAR(50) NOT NULL,
    term_id UUID NOT NULL REFERENCES term (id),
    name VARCHAR(255) NOT NULL,
    region VARCHAR(255),
    department_code VARCHAR(3), -- e.g. '75', '2A', '976', '099' for French residents abroad
    department_name VARCHAR(255),
    number INTEGER,
    valid_from DATE,
    valid_to DATE,
    PRIMARY KEY (id),
    UNIQUE (source_id, external_id)
);

CREATE INDEX idx_constituency_term ON constituency (term_id);

-- Communes and postcodes per district, from an INSEE / data.gouv dataset (not AMO).
-- Keyed on (chamber, department, number) rather than constituency_id because boundaries outlive legislatures,
-- so the mapping doesn't need reloading for every new term. Big cities are split: a postcode can map to several districts.
CREATE TABLE constituency_area (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    chamber_id UUID NOT NULL REFERENCES chamber (id),
    department_code VARCHAR(3) NOT NULL,
    constituency_number INTEGER NOT NULL,
    commune_code VARCHAR(5) NOT NULL,
    postcode VARCHAR(5),
    partial BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (chamber_id, department_code, constituency_number, commune_code, postcode)
);

-- The app looks up a citizen's representative by postcode or commune.
CREATE INDEX idx_constituency_area_postcode ON constituency_area (postcode);
CREATE INDEX idx_constituency_area_commune ON constituency_area (commune_code);

-- Mandates, groups and parties

-- A parliamentary seat (AN mandate of type ASSEMBLEE). The vote service references these by external id (PM…).
CREATE TABLE mandate (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    source_id UUID NOT NULL REFERENCES data_source (id),
    external_id VARCHAR(50) NOT NULL,
    person_id UUID NOT NULL REFERENCES person (id),
    term_id UUID NOT NULL REFERENCES term (id),
    constituency_id UUID REFERENCES constituency (id), -- null for list systems
    start_date DATE NOT NULL,
    end_date DATE,
    start_reason VARCHAR(255),
    end_reason VARCHAR(255),
    replaces_mandate_id UUID REFERENCES mandate (id),
    seat_number INTEGER,
    first_elected BOOLEAN,
    PRIMARY KEY (id),
    UNIQUE (source_id, external_id)
);

CREATE INDEX idx_mandate_person ON mandate (person_id);
CREATE INDEX idx_mandate_term ON mandate (term_id);
CREATE INDEX idx_mandate_constituency ON mandate (constituency_id);

CREATE TABLE political_group (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    source_id UUID NOT NULL REFERENCES data_source (id),
    external_id VARCHAR(50) NOT NULL,
    term_id UUID NOT NULL REFERENCES term (id),
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(50),
    color VARCHAR(7), -- '#RRGGBB'
    start_date DATE NOT NULL,
    end_date DATE,
    PRIMARY KEY (id),
    UNIQUE (source_id, external_id)
);

CREATE INDEX idx_political_group_term ON political_group (term_id);

CREATE TABLE group_membership (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    source_id UUID NOT NULL REFERENCES data_source (id),
    external_id VARCHAR(50) NOT NULL,
    person_id UUID NOT NULL REFERENCES person (id),
    group_id UUID NOT NULL REFERENCES political_group (id),
    role VARCHAR(20) NOT NULL CHECK (role IN ('MEMBER', 'AFFILIATED', 'PRESIDENT')),
    start_date DATE NOT NULL,
    end_date DATE,
    PRIMARY KEY (id),
    UNIQUE (source_id, external_id)
);

CREATE INDEX idx_group_membership_person ON group_membership (person_id);
CREATE INDEX idx_group_membership_group ON group_membership (group_id);

CREATE TABLE party (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    source_id UUID NOT NULL REFERENCES data_source (id),
    external_id VARCHAR(50) NOT NULL,
    jurisdiction_id UUID NOT NULL REFERENCES jurisdiction (id),
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(50),
    european_family VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE (source_id, external_id)
);

CREATE TABLE party_affiliation (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    source_id UUID NOT NULL REFERENCES data_source (id),
    external_id VARCHAR(50) NOT NULL,
    person_id UUID NOT NULL REFERENCES person (id),
    party_id UUID NOT NULL REFERENCES party (id),
    start_date DATE NOT NULL,
    end_date DATE,
    PRIMARY KEY (id),
    UNIQUE (source_id, external_id)
);

CREATE INDEX idx_party_affiliation_person ON party_affiliation (person_id);
CREATE INDEX idx_party_affiliation_party ON party_affiliation (party_id);

CREATE TABLE recusal (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    source_id UUID NOT NULL REFERENCES data_source (id),
    external_id VARCHAR(50) NOT NULL,
    person_id UUID NOT NULL REFERENCES person (id),
    scope TEXT,
    instance VARCHAR(255),
    target_type VARCHAR(255),
    target_description TEXT,
    published_at DATE,
    PRIMARY KEY (id),
    UNIQUE (source_id, external_id)
);

CREATE INDEX idx_recusal_person ON recusal (person_id);

-- Sync bookkeeping

CREATE TABLE sync_run (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    source_id UUID NOT NULL REFERENCES data_source (id),
    trigger VARCHAR(20) NOT NULL CHECK (trigger IN ('SCHEDULED', 'MANUAL')),
    archive_sha256 CHAR(64), -- lets a run skip an archive identical to the last successful one
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    status VARCHAR(20) NOT NULL CHECK (status IN ('RUNNING', 'SUCCEEDED', 'FAILED')),
    created INTEGER NOT NULL DEFAULT 0,
    updated INTEGER NOT NULL DEFAULT 0,
    unchanged INTEGER NOT NULL DEFAULT 0,
    failed INTEGER NOT NULL DEFAULT 0,
    error TEXT,
    PRIMARY KEY (id)
);

CREATE INDEX idx_sync_run_source_started ON sync_run (source_id, started_at);

-- One row per raw source file (e.g. one AMO acteur, organe or deport). Drives change detection for every table
-- derived from that file (an acteur file holds the person, contacts and all their mandates).
-- Records missing from a later archive keep their rows: last_seen_run_id shows they were not seen again.
CREATE TABLE raw_record (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    source_id UUID NOT NULL REFERENCES data_source (id),
    entity VARCHAR(50) NOT NULL, -- source vocabulary, e.g. 'acteur', 'organe', 'deport'
    external_id VARCHAR(50) NOT NULL,
    sha256 CHAR(64) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    s3_version_id VARCHAR(255),
    last_seen_run_id UUID NOT NULL REFERENCES sync_run (id),
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (source_id, entity, external_id)
);

-- Seed: France, the Assemblée nationale and its open data source. Terms are synced from the source.

INSERT INTO jurisdiction (iso_code, name, kind, default_language)
VALUES ('FR', 'France', 'COUNTRY', 'fr');

INSERT INTO data_source (jurisdiction_id, code, url, licence)
SELECT id, 'assemblee-nationale', 'https://data.assemblee-nationale.fr', 'Licence Ouverte / Open Licence'
FROM jurisdiction WHERE iso_code = 'FR';

INSERT INTO chamber (jurisdiction_id, code, name, kind, electoral_system, seats)
SELECT id, 'AN', 'Assemblée nationale', 'LOWER', 'CONSTITUENCY', 577
FROM jurisdiction WHERE iso_code = 'FR';
