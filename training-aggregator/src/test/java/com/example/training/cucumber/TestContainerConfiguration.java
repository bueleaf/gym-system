package com.example.training.cucumber;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;

@TestConfiguration
public class TestContainerConfiguration
{
    @Bean
    @ServiceConnection
    public MongoDBContainer mongoDb ()
    {
        return new MongoDBContainer("mongo:7");
    }
}
