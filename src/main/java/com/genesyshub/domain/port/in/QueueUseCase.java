package com.genesyshub.domain.port.in;

import com.genesyshub.domain.model.Queue;

import java.util.List;

public interface QueueUseCase {

    List<Queue> listAllQueues();

    Queue findQueueById(String queueId);

    List<Queue> findQueuesByMediaType(String mediaType);
}
