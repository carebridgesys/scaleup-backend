ALTER TABLE highlevel_pipeline_mapping
    ADD COLUMN agency_id BIGINT;

ALTER TABLE highlevel_pipeline_mapping
    ADD COLUMN attempting_contact_stage_id VARCHAR(150);

ALTER TABLE highlevel_pipeline_mapping
    ADD COLUMN contacted_stage_id VARCHAR(150);

ALTER TABLE highlevel_pipeline_mapping
    ADD COLUMN qualified_stage_id VARCHAR(150);

ALTER TABLE highlevel_pipeline_mapping
    ADD COLUMN routed_stage_id VARCHAR(150);

ALTER TABLE highlevel_pipeline_mapping
    ADD CONSTRAINT fk_highlevel_pipeline_mapping_agency
        FOREIGN KEY (agency_id)
            REFERENCES agencies(id);

ALTER TABLE highlevel_pipeline_mapping
DROP CONSTRAINT uq_highlevel_pipeline_mapping;

/*
 * Legacy/global mapping.
 *
 * This keeps your existing Internal CRM mappings working
 * while we migrate agencies one-by-one.
 */
CREATE UNIQUE INDEX uq_highlevel_pipeline_mapping_global
    ON highlevel_pipeline_mapping (
                                   location_id,
                                   lead_type
        )
    WHERE agency_id IS NULL;

/*
 * New production model.
 */
CREATE UNIQUE INDEX uq_highlevel_pipeline_mapping_agency
    ON highlevel_pipeline_mapping (
                                   agency_id,
                                   location_id,
                                   lead_type
        )
    WHERE agency_id IS NOT NULL;

CREATE INDEX idx_highlevel_pipeline_mapping_agency
    ON highlevel_pipeline_mapping(agency_id);