CREATE TABLE highlevel_custom_field_mapping (
                                                id BIGSERIAL PRIMARY KEY,

                                                location_id VARCHAR(150) NOT NULL,

                                                field_key VARCHAR(255) NOT NULL,

                                                external_field_id VARCHAR(150) NOT NULL,

                                                field_name VARCHAR(255),

                                                data_type VARCHAR(100),

                                                active BOOLEAN NOT NULL DEFAULT TRUE,

                                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                                CONSTRAINT uq_highlevel_custom_field_mapping
                                                    UNIQUE (location_id, field_key)
);

CREATE INDEX idx_highlevel_custom_field_location
    ON highlevel_custom_field_mapping(location_id);

CREATE INDEX idx_highlevel_custom_field_key
    ON highlevel_custom_field_mapping(field_key);