package com.genesyshub.infrastructure.web;

import com.genesyshub.domain.model.DomainException;
import com.genesyshub.domain.port.in.QueueUseCase;
import com.genesyshub.infrastructure.web.controller.QueueController;
import com.genesyshub.infrastructure.web.mapper.DomainToDtoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QueueController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QueueUseCase queueUseCase;

    @MockBean
    private DomainToDtoMapper domainToDtoMapper;

    @Test
    void handleDomainException_returnsNotFound_forQueueNotFound() throws Exception {
        when(queueUseCase.findQueueById(anyString()))
                .thenThrow(new DomainException(DomainException.ErrorCode.QUEUE_NOT_FOUND, "Queue not found: q1"));

        mockMvc.perform(get("/api/v1/queues/q1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("QUEUE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Queue not found: q1"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/queues/q1"));
    }

    @Test
    void handleDomainException_returnsUnauthorized_forAuthFailed() throws Exception {
        when(queueUseCase.findQueueById(anyString()))
                .thenThrow(new DomainException(DomainException.ErrorCode.GENESYS_AUTH_FAILED, "Auth failed"));

        mockMvc.perform(get("/api/v1/queues/any"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("GENESYS_AUTH_FAILED"));
    }

    @Test
    void handleDomainException_returnsGatewayError_forApiError() throws Exception {
        when(queueUseCase.findQueueById(anyString()))
                .thenThrow(new DomainException(DomainException.ErrorCode.GENESYS_API_ERROR, "Upstream error"));

        mockMvc.perform(get("/api/v1/queues/any"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value("GENESYS_API_ERROR"));
    }

    @Test
    void handleDomainException_returnsTooManyRequests_forRateLimit() throws Exception {
        when(queueUseCase.findQueueById(anyString()))
                .thenThrow(new DomainException(DomainException.ErrorCode.RATE_LIMIT_EXCEEDED, "Rate limit exceeded"));

        mockMvc.perform(get("/api/v1/queues/any"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.errorCode").value("RATE_LIMIT_EXCEEDED"));
    }

    @Test
    void handleGenericException_returnsInternalServerError() throws Exception {
        when(queueUseCase.findQueueById(anyString()))
                .thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(get("/api/v1/queues/any"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"));
    }
}
