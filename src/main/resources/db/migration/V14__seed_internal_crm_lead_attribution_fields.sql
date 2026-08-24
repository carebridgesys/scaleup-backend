INSERT INTO highlevel_custom_field_mapping (
    location_id,
    field_key,
    external_field_id,
    field_name,
    data_type,
    active
)
VALUES
    (
        'gDbcmwKdIsVIGEgcAfZa',
        'contact.agency_name',
        'fxrooK6YJSXOHCgKk1PO',
        'Agency Name',
        'TEXT',
        TRUE
    ),
    (
        'gDbcmwKdIsVIGEgcAfZa',
        'contact.lead_type',
        'ubexYjSTs1wNl1P4zpp3',
        'Lead Type',
        'SINGLE_OPTIONS',
        TRUE
    )
    ON CONFLICT (location_id, field_key)
DO UPDATE SET
    external_field_id = EXCLUDED.external_field_id,
           field_name = EXCLUDED.field_name,
           data_type = EXCLUDED.data_type,
           active = TRUE,
           updated_at = CURRENT_TIMESTAMP;