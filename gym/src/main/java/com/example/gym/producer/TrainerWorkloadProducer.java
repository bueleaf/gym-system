package com.example.gym.producer;

import com.example.gym.dto.request.TrainerWorkloadEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class TrainerWorkloadProducer
{
    private final ObjectMapper objectMapper;
    private final JmsTemplate jmsTemplate;
    private final String queueName;

    public TrainerWorkloadProducer(ObjectMapper objectMapper,
                                   JmsTemplate jmsTemplate,
                                   @Value("${messaging.trainer-workload-queue}")
                                   String queueName)
    {
        this.objectMapper = objectMapper;
        this.jmsTemplate = jmsTemplate;
        this.queueName = queueName;
    }

    public void send(TrainerWorkloadEvent event)
    {
        try
        {
            String json = objectMapper.writeValueAsString(event);
            jmsTemplate.convertAndSend(queueName, json);
        }
        catch (JsonProcessingException ex)
        {
            throw new IllegalStateException("Failed to serialize trainer workload event", ex);
        }
    }
}

