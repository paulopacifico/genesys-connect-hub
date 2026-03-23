package com.genesyshub.application.service;

import com.genesyshub.domain.model.DomainException;
import com.genesyshub.domain.model.Queue;
import com.genesyshub.domain.port.in.QueueUseCase;
import com.genesyshub.domain.port.out.QueuePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService implements QueueUseCase {

    private final QueuePort queuePort;

    @Override
    @Cacheable("queues")
    public List<Queue> listAllQueues() {
        List<Queue> queues = queuePort.fetchAllQueues();
        log.info("Listed {} queues", queues.size());
        return queues;
    }

    @Override
    public Queue findQueueById(String queueId) {
        return queuePort.fetchQueueById(queueId)
                .orElseThrow(() -> new DomainException(
                        DomainException.ErrorCode.QUEUE_NOT_FOUND,
                        "Queue not found: " + queueId));
    }

    @Override
    public List<Queue> findQueuesByMediaType(String mediaType) {
        return listAllQueues().stream()   // reuses cached result
                .filter(q -> q.mediaTypes() != null && q.mediaTypes().contains(mediaType))
                .toList();
    }
}
