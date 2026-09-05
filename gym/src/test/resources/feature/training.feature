Feature: Training management

    Scenario: Create a valid training
        Given valid trainer and trainee data is provided
        When valid training is created
        Then training creation succeeds

    Scenario: Reject invalid training
        Given invalid training is provided
        When training creation is attempted
        Then request is rejected