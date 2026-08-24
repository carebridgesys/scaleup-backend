INSERT INTO highlevel_pipeline_mapping (
    location_id,
    lead_type,
    pipeline_id,
    initial_stage_id,
    pipeline_name,
    initial_stage_name,
    active
)
VALUES (
           'rpelZN1piHfi4cRKl2VP',
           'CAREGIVER',
           'il1RzgGe5CE27sE3pwWW',
           '2e41ffd6-812d-4f07-baac-d796910a8e9a',
           'Caregiver Acquisition',
           'New Applicant',
           TRUE
       )
    ON CONFLICT (location_id, lead_type)
DO UPDATE SET
    pipeline_id = EXCLUDED.pipeline_id,
           initial_stage_id = EXCLUDED.initial_stage_id,
           pipeline_name = EXCLUDED.pipeline_name,
           initial_stage_name = EXCLUDED.initial_stage_name,
           active = TRUE,
           updated_at = CURRENT_TIMESTAMP;