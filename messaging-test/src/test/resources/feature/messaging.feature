Feature: Trainer workload synchronization

    Background:
            Given trainee and trainer exist in Gym
            And no workload exists for the trainer

    Scenario: Creating training updates trainer workload
        When 60 minute training is created in Gym service
        Then Aggregator service contains 60 minutes of workload

    Scenario: Rejected training does not update workload
        When invalid training creation is attempted
        Then Aggregator service does not update workload for the trainer