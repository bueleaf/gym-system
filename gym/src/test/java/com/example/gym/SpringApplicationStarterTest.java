package com.example.gym;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class SpringApplicationStarterTest {

    @Test
    void isConfiguredAsSpringBootApplication() {
        assertThat(SpringApplicationStarter.class)
                .hasAnnotation(SpringBootApplication.class);
    }
}
