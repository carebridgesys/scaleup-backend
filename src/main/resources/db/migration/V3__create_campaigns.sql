CREATE TABLE campaigns (
                           id BIGSERIAL PRIMARY KEY,
                           public_id UUID NOT NULL UNIQUE,

                           agency_id BIGINT NOT NULL,

                           name VARCHAR(200) NOT NULL,
                           slug VARCHAR(150) NOT NULL,

                           campaign_type VARCHAR(30) NOT NULL,

                           source VARCHAR(100),
                           external_campaign_id VARCHAR(150),

                           landing_page_key VARCHAR(150) NOT NULL UNIQUE,

                           active BOOLEAN NOT NULL DEFAULT TRUE,

                           start_date TIMESTAMP,
                           end_date TIMESTAMP,

                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_campaigns_agency
                               FOREIGN KEY (agency_id)
                                   REFERENCES agencies(id),

                           CONSTRAINT uq_campaign_agency_slug
                               UNIQUE (agency_id, slug),

                           CONSTRAINT chk_campaign_type
                               CHECK (
                                   campaign_type IN (
                                                     'CLIENT',
                                                     'CAREGIVER'
                                       )
                                   ),

                           CONSTRAINT chk_campaign_dates
                               CHECK (
                                   end_date IS NULL
                                       OR start_date IS NULL
                                       OR end_date >= start_date
                                   )
);


ALTER TABLE leads
    ADD COLUMN campaign_id BIGINT;


ALTER TABLE leads
    ADD CONSTRAINT fk_leads_campaign
        FOREIGN KEY (campaign_id)
            REFERENCES campaigns(id);


CREATE INDEX idx_campaigns_agency_id
    ON campaigns(agency_id);

CREATE INDEX idx_campaigns_campaign_type
    ON campaigns(campaign_type);

CREATE INDEX idx_campaigns_active
    ON campaigns(active);

CREATE INDEX idx_campaigns_external_campaign_id
    ON campaigns(external_campaign_id);

CREATE INDEX idx_leads_campaign_id
    ON leads(campaign_id);