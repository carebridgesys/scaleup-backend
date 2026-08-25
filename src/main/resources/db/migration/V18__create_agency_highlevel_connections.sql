CREATE TABLE agency_highlevel_connections (
                                              id BIGSERIAL PRIMARY KEY,

                                              agency_id BIGINT NOT NULL,

                                              location_id VARCHAR(150) NOT NULL,

                                              access_token_encrypted TEXT NOT NULL,

                                              auth_type VARCHAR(30) NOT NULL DEFAULT 'PRIVATE_TOKEN',

                                              connection_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

                                              token_expires_at TIMESTAMP NULL,

                                              refresh_token_encrypted TEXT NULL,

                                              scopes TEXT NULL,

                                              created_at TIMESTAMP NOT NULL,
                                              updated_at TIMESTAMP NOT NULL,

                                              CONSTRAINT fk_agency_highlevel_connection_agency
                                                  FOREIGN KEY (agency_id)
                                                      REFERENCES agencies(id),

                                              CONSTRAINT uq_agency_highlevel_connection_agency
                                                  UNIQUE (agency_id),

                                              CONSTRAINT uq_agency_highlevel_connection_location
                                                  UNIQUE (location_id),

                                              CONSTRAINT chk_agency_highlevel_connection_auth_type
                                                  CHECK (
                                                      auth_type IN (
                                                                    'PRIVATE_TOKEN',
                                                                    'OAUTH'
                                                          )
                                                      ),

                                              CONSTRAINT chk_agency_highlevel_connection_status
                                                  CHECK (
                                                      connection_status IN (
                                                                            'ACTIVE',
                                                                            'DISCONNECTED',
                                                                            'ERROR'
                                                          )
                                                      )
);

CREATE INDEX idx_agency_highlevel_connections_status
    ON agency_highlevel_connections(connection_status);