package com.genesyshub.domain.port.out;

import com.genesyshub.domain.model.Queue;

import java.util.List;
import java.util.Optional;

public interface QueuePort {

    List<Queue> fetchAllQueues();

    Optional<Queue> fetchQueueById(String queueId);
}
