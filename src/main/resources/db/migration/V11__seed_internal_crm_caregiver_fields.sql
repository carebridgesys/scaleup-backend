INSERT INTO highlevel_custom_field_mapping (
    location_id,
    field_key,
    external_field_id,
    field_name,
    data_type
)
VALUES
    (
        'gDbcmwKdIsVIGEgcAfZa',
        'contact.years_of_experience',
        'LsUr4qmLyaJN1U3bajI3',
        'Years of Experience',
        'NUMERICAL'
    ),
    (
        'gDbcmwKdIsVIGEgcAfZa',
        'contact.certification',
        'PyX3OSjbyZqSYpbb3KAy',
        'Certification',
        'LARGE_TEXT'
    ),
    (
        'gDbcmwKdIsVIGEgcAfZa',
        'contact.availability',
        'Z79xKAm1PllJHlJ9HAaW',
        'Availability',
        'SINGLE_OPTIONS'
    ),
    (
        'gDbcmwKdIsVIGEgcAfZa',
        'contact.transportation',
        'wCPjSXpjdWZZQTY19BNN',
        'Transportation',
        'SINGLE_OPTIONS'
    ),
    (
        'gDbcmwKdIsVIGEgcAfZa',
        'contact.preferred_schedule',
        'NHgZl8TAFdodHglRCdDo',
        'Preferred Schedule',
        'SINGLE_OPTIONS'
    ),
    (
        'gDbcmwKdIsVIGEgcAfZa',
        'contact.desired_hours_per_week',
        'tPKO8LvStOkEnOOvtJry',
        'Desired Hours Per Week',
        'NUMERICAL'
    ),
    (
        'gDbcmwKdIsVIGEgcAfZa',
        'contact.service_area',
        '42UdJ6hCIw20Z35wcwB0',
        'Service Area',
        'TEXT'
    ),
    (
        'gDbcmwKdIsVIGEgcAfZa',
        'contact.background_status_check',
        'VFmt4dHWX0FA3mCYPUt6',
        'Background Status Check',
        'SINGLE_OPTIONS'
    ),
    (
        'gDbcmwKdIsVIGEgcAfZa',
        'contact.interview_status',
        'weDWcKTRCIXFo4w7NVyq',
        'Interview Status',
        'SINGLE_OPTIONS'
    ),
    (
        'gDbcmwKdIsVIGEgcAfZa',
        'contact.ai_screening_score',
        'dVxF97w5DzW6G2VHUbWh',
        'AI Screening Score',
        'NUMERICAL'
    ),
    (
        'gDbcmwKdIsVIGEgcAfZa',
        'contact.ai_screening_summary',
        'nBfOxibXimoeBhUEDhP6',
        'AI Screening Summary',
        'LARGE_TEXT'
    )
    ON CONFLICT (location_id, field_key)
DO UPDATE SET
    external_field_id = EXCLUDED.external_field_id,
           field_name = EXCLUDED.field_name,
           data_type = EXCLUDED.data_type,
           active = TRUE,
           updated_at = CURRENT_TIMESTAMP;