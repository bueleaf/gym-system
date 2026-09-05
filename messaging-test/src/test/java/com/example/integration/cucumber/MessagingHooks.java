package com.example.integration.cucumber;


import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;

public class MessagingHooks
{
    @BeforeAll
    public static void start()
    {
        MessagingEnvironment.start();
    }

    @AfterAll
    public static void stop()
    {
        MessagingEnvironment.stop();
    }
}
