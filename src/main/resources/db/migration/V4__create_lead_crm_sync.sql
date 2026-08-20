CREATE TABLE lead_crm_sync (
                               id BIGSERIAL PRIMARY KEY,

                               lead_id BIGINT NOT NULL,

                               destination VARCHAR(30) NOT NULL,

                               sync_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

                               external_contact_id VARCHAR(150),
                               external_opportunity_id VARCHAR(150),

                               attempt_count INTEGER NOT NULL DEFAULT 0,

                               last_attempt_at TIMESTAMP,
                               next_attempt_at TIMESTAMP,

                               last_error TEXT,

                               synced_at TIMESTAMP,

                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_lead_crm_sync_lead
                                   FOREIGN KEY (lead_id)
                                       REFERENCES leads(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT uq_lead_crm_sync_destination
                                   UNIQUE (lead_id, destination),

                               CONSTRAINT chk_lead_crm_sync_destination
                                   CHECK (
                                       destination IN (
                                                       'INTERNAL_CRM',
                                                       'AGENCY_CRM'
                                           )
                                       ),

                               CONSTRAINT chk_lead_crm_sync_status
                                   CHECK (
                                       sync_status IN (
                                                       'PENDING',
                                                       'PROCESSING',
                                                       'SYNCED',
                                                       'FAILED',
                                                       'NOT_REQUIRED'
                                           )
                                       ),

                               CONSTRAINT chk_lead_crm_sync_attempt_count
                                   CHECK (attempt_count >= 0)
);


CREATE INDEX idx_lead_crm_sync_lead_id
    ON lead_crm_sync(lead_id);

CREATE INDEX idx_lead_crm_sync_status
    ON lead_crm_sync(sync_status);

CREATE INDEX idx_lead_crm_sync_destination_status
    ON lead_crm_sync(destination, sync_status);

CREATE INDEX idx_lead_crm_sync_next_attempt
    ON lead_crm_sync(next_attempt_at);