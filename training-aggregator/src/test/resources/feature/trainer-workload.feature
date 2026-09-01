Feature: Trainer workload add

    Scenario: Add workload for a trainer
        Given no workload exists for trainer "trainer.test"
        When ADD workload event of 60 minutes is processed
        Then trainer workload should contain 60 minutes

    Scenario: Reject workload event without trainer username
        Given workload event without trainer username
        When workload event is processed
        Then no trainer workload is created