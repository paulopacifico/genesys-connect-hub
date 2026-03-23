-- Conversation metrics spanning different times and queues
INSERT INTO conversation_metrics (conversation_id, queue_id, agent_id, media_type, direction,
                                  start_time, end_time, handle_time_seconds, abandoned)
VALUES
    ('conv-in-range-1', 'queue-001', 'agent-001', 'voice', 'inbound',
     '2024-01-10 09:00:00+00', '2024-01-10 09:05:00+00', 300, false),
    ('conv-in-range-2', 'queue-001', 'agent-002', 'voice', 'inbound',
     '2024-01-15 10:00:00+00', '2024-01-15 10:02:00+00', 120, true),
    ('conv-in-range-3', 'queue-001', 'agent-001', 'chat',  'inbound',
     '2024-01-20 14:00:00+00', '2024-01-20 14:08:00+00', 480, false),
    ('conv-out-of-range', 'queue-001', 'agent-001', 'voice', 'inbound',
     '2024-02-05 09:00:00+00', '2024-02-05 09:03:00+00', 180, false),
    ('conv-other-queue', 'queue-002', 'agent-003', 'voice', 'inbound',
     '2024-01-12 11:00:00+00', '2024-01-12 11:04:00+00', 240, true);

-- Webhook events
INSERT INTO webhook_events (event_id, topic_name, version, payload, received_at, processed, processed_at)
VALUES
    ('event-processed-1', 'v2.routing.queue.conversations.voice', '2',
     '{"key":"value1"}', '2024-01-10 09:00:00+00', true,  '2024-01-10 09:00:01+00'),
    ('event-unprocessed-1', 'v2.routing.queue.conversations.chat', '2',
     '{"key":"value2"}', '2024-01-11 10:00:00+00', false, null),
    ('event-unprocessed-2', 'v2.routing.queue.conversations.email', '2',
     '{"key":"value3"}', '2024-01-12 11:00:00+00', false, null);
