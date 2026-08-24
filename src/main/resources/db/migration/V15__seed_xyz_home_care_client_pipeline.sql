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
           'CLIENT',
           'cM6UNB87AMGrfLt4e7qq',
           '526ef396-6039-4ce5-bae8-9c53fa357b21',
           'Client Acquisition',
           'New Lead',
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