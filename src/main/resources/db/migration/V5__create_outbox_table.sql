CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL, -- 'USER_CREATED', 'USER_UPDATED', 'USER_DELETED', 'USER_ACTIVATED', 'USER_DEACTIVATED'
    payload JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'PROCESSED', 'FAILED'
    retry_count INT DEFAULT 0,
    last_error TEXT
);

CREATE INDEX idx_outbox_pending ON outbox_events(status, created_at);