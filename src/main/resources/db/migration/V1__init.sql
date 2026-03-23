-- Initial schema placeholder
-- Add your domain tables here

CREATE TABLE IF NOT EXISTS schema_version_audit (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
