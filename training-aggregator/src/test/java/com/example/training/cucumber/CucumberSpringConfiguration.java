package com.example.training.cucumber;

import com.example.training.consumer.TrainerWorkloadListener;
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
}
