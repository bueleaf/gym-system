package com.example.gym.cucumber;

import com.example.gym.producer.TrainerWorkloadProducer;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@ActiveProfiles("test")
@Import(TestContainerConfiguration.class)
@AutoConfigureMockMvc
@SpringBootTest
public class CucumberSpringConfiguration
{
    @MockitoBean
    public TrainerWorkloadProducer trainerWorkloadProducer;
}
