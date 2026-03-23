CREATE TABLE conversation_metrics (
    id                    BIGSERIAL PRIMARY KEY,
    conversation_id       VARCHAR(255) NOT NULL UNIQUE,
    queue_id              VARCHAR(255),
    agent_id              VARCHAR(255),
    media_type            VARCHAR(100),
    direction             VARCHAR(50),
    start_time            TIMESTAMP WITH TIME ZONE,
    end_time              TIMESTAMP WITH TIME ZONE,
    handle_time_seconds   BIGINT,
    abandoned             BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
