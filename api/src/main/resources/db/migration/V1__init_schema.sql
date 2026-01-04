CREATE TABLE parties (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    external_id VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE representatives (
    id  UUID NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    external_id VARCHAR(255),
    party_id UUID NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_party_id FOREIGN KEY (party_id) REFERENCES parties(id)
);

CREATE TABLE party_stats (
    id UUID NOT NULL,
    total_votes INTEGER,
    total_laws INTEGER,
    PRIMARY KEY (id),
    CONSTRAINT fk_id FOREIGN KEY (id) REFERENCES parties(id)
);

CREATE TABLE representative_stats (
    id UUID NOT NULL,
    total_votes INTEGER,
    total_laws INTEGER,
    PRIMARY KEY (id),
    CONSTRAINT fk_id FOREIGN KEY (id) REFERENCES representatives(id)
);