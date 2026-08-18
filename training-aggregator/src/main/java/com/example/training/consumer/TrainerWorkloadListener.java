package com.example.training.consumer;

import com.example.training.dto.request.TrainerWorkloadEvent;
import com.example.training.service.TrainerWorkloadService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class TrainerWorkloadListener
{
    private final TrainerWorkloadService trainerWorkloadService;
    private final ObjectMapper objectMapper;

    public TrainerWorkloadListener(TrainerWorkloadService trainerWorkloadService,
                                   ObjectMapper objectMapper)
    {
        this.trainerWorkloadService = trainerWorkloadService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination="${messaging.trainer-workload-queue}")
    public void receive(String json)
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
            throw new IllegalStateException(
                    "Failed to serialize trainer workload event",
                    ex
                    );
        }
    }
}
