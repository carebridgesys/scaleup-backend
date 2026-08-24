CREATE TABLE lead_ai_transcripts (
                                     id BIGSERIAL PRIMARY KEY,

                                     lead_id BIGINT NOT NULL,

                                     provider VARCHAR(100) NOT NULL,

                                     external_call_id VARCHAR(150),

                                     agent_type VARCHAR(30) NOT NULL,

                                     transcript TEXT NOT NULL,

                                     summary TEXT,

                                     received_at TIMESTAMP NOT NULL,

                                     created_at TIMESTAMP NOT NULL,

                                     CONSTRAINT fk_lead_ai_transcript_lead
                                         FOREIGN KEY (lead_id)
                                             REFERENCES leads(id)
                                             ON DELETE CASCADE
);

CREATE INDEX idx_lead_ai_transcripts_lead_id
    ON lead_ai_transcripts(lead_id);

CREATE INDEX idx_lead_ai_transcripts_external_call_id
    ON lead_ai_transcripts(external_call_id);