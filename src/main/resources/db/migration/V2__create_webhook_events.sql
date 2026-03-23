CREATE TABLE webhook_events (
    id            BIGSERIAL PRIMARY KEY,
    event_id      VARCHAR(255) NOT NULL UNIQUE,
    topic_name    VARCHAR(255),
    version       VARCHAR(50),
    payload       JSONB,
    received_at   TIMESTAMP WITH TIME ZONE,
    processed     BOOLEAN      NOT NULL DEFAULT FALSE,
    processed_at  TIMESTAMP WITH TIME ZONE
);
