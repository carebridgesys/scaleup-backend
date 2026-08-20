CREATE TABLE highlevel_pipeline_mapping (
                                            id BIGSERIAL PRIMARY KEY,

                                            location_id VARCHAR(150) NOT NULL,

                                            lead_type VARCHAR(30) NOT NULL,

                                            pipeline_id VARCHAR(150) NOT NULL,

                                            initial_stage_id VARCHAR(150) NOT NULL,

                                            pipeline_name VARCHAR(200),

                                            initial_stage_name VARCHAR(200),

                                            active BOOLEAN NOT NULL DEFAULT TRUE,

                                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                            CONSTRAINT uq_highlevel_pipeline_mapping
                                                UNIQUE (location_id, lead_type),

                                            CONSTRAINT chk_highlevel_pipeline_mapping_lead_type
                                                CHECK (
                                                    lead_type IN (
                                                                  'CLIENT',
                                                                  'CAREGIVER'
                                                        )
                                                    )
);

CREATE INDEX idx_highlevel_pipeline_mapping_location
    ON highlevel_pipeline_mapping(location_id);