-- conversation_metrics indexes
CREATE INDEX idx_cm_queue_id    ON conversation_metrics (queue_id);
CREATE INDEX idx_cm_start_time  ON conversation_metrics (start_time);
CREATE INDEX idx_cm_abandoned   ON conversation_metrics (abandoned) WHERE abandoned = TRUE;

-- webhook_events indexes
CREATE INDEX idx_we_event_id    ON webhook_events (event_id);
CREATE INDEX idx_we_processed   ON webhook_events (processed) WHERE processed = FALSE;
