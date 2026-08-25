ALTER TABLE lead_ai_transcripts
    ADD COLUMN event_key VARCHAR(64);

CREATE UNIQUE INDEX uq_lead_ai_transcripts_event_key
    ON lead_ai_transcripts (event_key)
    WHERE event_key IS NOT NULL;