CREATE TABLE delegations (
                             id UUID PRIMARY KEY,
                             delegator_id UUID NOT NULL REFERENCES users(id),
                             delegatee_id UUID NOT NULL REFERENCES users(id),
                             delegated_roles JSONB NOT NULL,
                             start_time TIMESTAMP NOT NULL,
                             end_time TIMESTAMP NOT NULL,
                             status VARCHAR(20) NOT NULL, -- ACTIVE, EXPIRED, SCHEDULED
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_delegation_status ON delegations(status);