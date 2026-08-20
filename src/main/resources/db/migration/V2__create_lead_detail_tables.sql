CREATE TABLE client_lead_details (
                                     id BIGSERIAL PRIMARY KEY,

                                     lead_id BIGINT NOT NULL UNIQUE,

                                     service_needed VARCHAR(100),
                                     care_start_timeline VARCHAR(100),
                                     payer_type VARCHAR(100),
                                     decision_maker VARCHAR(100),

                                     ai_qualification_score INTEGER,
                                     ai_summary TEXT,

                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT fk_client_lead_details_lead
                                         FOREIGN KEY (lead_id)
                                             REFERENCES leads(id)
                                             ON DELETE CASCADE,

                                     CONSTRAINT chk_client_ai_qualification_score
                                         CHECK (
                                             ai_qualification_score IS NULL
                                                 OR (
                                                 ai_qualification_score >= 0
                                                     AND ai_qualification_score <= 100
                                                 )
                                             )
);


CREATE TABLE caregiver_lead_details (
                                        id BIGSERIAL PRIMARY KEY,

                                        lead_id BIGINT NOT NULL UNIQUE,

                                        years_experience INTEGER,
                                        certifications TEXT,
                                        availability VARCHAR(100),
                                        transportation VARCHAR(100),
                                        preferred_schedule VARCHAR(100),
                                        desired_hours_per_week INTEGER,
                                        service_area VARCHAR(255),
                                        background_check_status VARCHAR(100),
                                        interview_status VARCHAR(100),

                                        ai_screening_score INTEGER,
                                        ai_screening_summary TEXT,

                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                        CONSTRAINT fk_caregiver_lead_details_lead
                                            FOREIGN KEY (lead_id)
                                                REFERENCES leads(id)
                                                ON DELETE CASCADE,

                                        CONSTRAINT chk_caregiver_years_experience
                                            CHECK (
                                                years_experience IS NULL
                                                    OR years_experience >= 0
                                                ),

                                        CONSTRAINT chk_caregiver_desired_hours
                                            CHECK (
                                                desired_hours_per_week IS NULL
                                                    OR (
                                                    desired_hours_per_week >= 0
                                                        AND desired_hours_per_week <= 168
                                                    )
                                                ),

                                        CONSTRAINT chk_caregiver_ai_screening_score
                                            CHECK (
                                                ai_screening_score IS NULL
                                                    OR (
                                                    ai_screening_score >= 0
                                                        AND ai_screening_score <= 100
                                                    )
                                                )
);


CREATE INDEX idx_client_lead_details_lead_id
    ON client_lead_details(lead_id);

CREATE INDEX idx_caregiver_lead_details_lead_id
    ON caregiver_lead_details(lead_id);