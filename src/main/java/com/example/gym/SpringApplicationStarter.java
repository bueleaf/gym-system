package com.example.gym;

import com.example.gym.config.JpaConfig;
import com.example.gym.facade.GymFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringApplicationStarter {
    private static final Logger logger = LoggerFactory.getLogger(SpringApplicationStarter.class);

    public static void main(String[] args) {
        logger.info("Starting Gym CRM Application");

        ApplicationContext context = new AnnotationConfigApplicationContext(JpaConfig.class);
        GymFacade facade = context.getBean(GymFacade.class);

        logger.info("Application started successfully");
        //facade.run();
        //View can be implemented for facade to run through console
        logger.info("Application finished");
    }
}
