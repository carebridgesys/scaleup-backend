CREATE TABLE lead_ai_contact (
                                 id BIGSERIAL PRIMARY KEY,

                                 lead_id BIGINT NOT NULL UNIQUE,

                                 status VARCHAR(30) NOT NULL,

                                 provider VARCHAR(100),

                                 external_call_id VARCHAR(150),

                                 attempt_count INTEGER NOT NULL DEFAULT 0,

                                 started_at TIMESTAMP,

                                 completed_at TIMESTAMP,

                                 last_error TEXT,

                                 transcript_reference VARCHAR(500),

                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_lead_ai_contact_lead
                                     FOREIGN KEY (lead_id)
                                         REFERENCES leads(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT chk_lead_ai_contact_status
                                     CHECK (
                                         status IN (
                                                    'PENDING',
                                                    'IN_PROGRESS',
                                                    'COMPLETED',
                                                    'FAILED',
                                                    'CANCELLED'
                                             )
                                         ),

                                 CONSTRAINT chk_lead_ai_contact_attempt_count
                                     CHECK (attempt_count >= 0)
);

CREATE INDEX idx_lead_ai_contact_status
    ON lead_ai_contact(status);

CREATE INDEX idx_lead_ai_contact_external_call_id
    ON lead_ai_contact(external_call_id);