package com.example.training.consumer;

import com.example.training.dto.request.TrainerWorkloadEvent;
import com.example.training.exception.InvalidWorkloadException;
import com.example.training.service.TrainerWorkloadService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class TrainerWorkloadListener
{

    private final static Logger LOG =
            LoggerFactory.getLogger(TrainerWorkloadListener.class);

    private final TrainerWorkloadService trainerWorkloadService;
    private final ObjectMapper objectMapper;

    public TrainerWorkloadListener(TrainerWorkloadService trainerWorkloadService,
                                   ObjectMapper objectMapper)
    {
        this.trainerWorkloadService = trainerWorkloadService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination="${messaging.trainer-workload-queue}")
    public void receive(String json, Message message)
    {
        try
        {
            TrainerWorkloadEvent event = objectMapper.readValue(
                    json,
                    TrainerWorkloadEvent.class
            );
            trainerWorkloadService.updateWorkload(event);
        }
        catch (JsonProcessingException ex)
        {
            LOG.error(
                    "Invalid trainer workload message {}",
                    json,
                    ex
            );

            throw new IllegalStateException(
                    "Failed to deserialize trainer workload event",
                    ex
                    );
        }
        catch (InvalidWorkloadException | EntityNotFoundException ex)
        {
            LOG.error("Failed to process workload message: {}", json, ex);
            throw ex;
        }
    }
}
