package com.example.gym.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@ActiveProfiles("test")
@SpringBootTest
public class CucumberSpringConfiguration {
}
