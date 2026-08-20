CREATE TABLE agencies (
                          id BIGSERIAL PRIMARY KEY,
                          public_id UUID NOT NULL UNIQUE,

                          name VARCHAR(150) NOT NULL,
                          slug VARCHAR(120) NOT NULL UNIQUE,

                          highlevel_location_id VARCHAR(150),
                          highlevel_sync_enabled BOOLEAN NOT NULL DEFAULT FALSE,

                          active BOOLEAN NOT NULL DEFAULT TRUE,

                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE leads (
                       id BIGSERIAL PRIMARY KEY,
                       public_id UUID NOT NULL UNIQUE,

                       agency_id BIGINT NOT NULL,

                       lead_type VARCHAR(30) NOT NULL,

                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100),

                       phone VARCHAR(30) NOT NULL,
                       email VARCHAR(255),
                       zip_code VARCHAR(20),

                       preferred_contact_method VARCHAR(30),

                       source VARCHAR(100),
                       campaign_name VARCHAR(200),

                       status VARCHAR(50) NOT NULL DEFAULT 'NEW',

                       consent_given BOOLEAN NOT NULL DEFAULT FALSE,
                       consent_timestamp TIMESTAMP,

                       highlevel_contact_id VARCHAR(150),
                       highlevel_opportunity_id VARCHAR(150),
                       highlevel_sync_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_leads_agency
                           FOREIGN KEY (agency_id)
                               REFERENCES agencies(id),

                       CONSTRAINT chk_lead_type
                           CHECK (lead_type IN ('CLIENT', 'CAREGIVER')),

                       CONSTRAINT chk_highlevel_sync_status
                           CHECK (
                               highlevel_sync_status IN (
                                                         'PENDING',
                                                         'SYNCED',
                                                         'FAILED',
                                                         'NOT_REQUIRED'
                                   )
                               )
);

CREATE INDEX idx_leads_agency_id
    ON leads(agency_id);

CREATE INDEX idx_leads_phone
    ON leads(phone);

CREATE INDEX idx_leads_email
    ON leads(email);

CREATE INDEX idx_leads_status
    ON leads(status);

CREATE INDEX idx_leads_created_at
    ON leads(created_at);