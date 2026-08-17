package com.example.training.exception;

public class InvalidWorkloadException extends RuntimeException
{
    public InvalidWorkloadException(String message)
    {
        super(message);
    }
}
