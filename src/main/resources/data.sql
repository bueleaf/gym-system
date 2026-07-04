INSERT INTO training_types (training_type_name)
SELECT 'Yoga'              WHERE NOT EXISTS (SELECT 1 FROM training_types WHERE training_type_name = 'Yoga');

INSERT INTO training_types (training_type_name)
SELECT 'Cardio'            WHERE NOT EXISTS (SELECT 1 FROM training_types WHERE training_type_name = 'Cardio');

INSERT INTO training_types (training_type_name)
SELECT 'Strength Training' WHERE NOT EXISTS (SELECT 1 FROM training_types WHERE training_type_name = 'Strength Training');

INSERT INTO training_types (training_type_name)
SELECT 'Pilates'           WHERE NOT EXISTS (SELECT 1 FROM training_types WHERE training_type_name = 'Pilates');

INSERT INTO training_types (training_type_name)
SELECT 'CrossFit'          WHERE NOT EXISTS (SELECT 1 FROM training_types WHERE training_type_name = 'CrossFit');

INSERT INTO training_types (training_type_name)
SELECT 'Boxing'            WHERE NOT EXISTS (SELECT 1 FROM training_types WHERE training_type_name = 'Boxing');

INSERT INTO training_types (training_type_name)
SELECT 'Swimming'          WHERE NOT EXISTS (SELECT 1 FROM training_types WHERE training_type_name = 'Swimming');

INSERT INTO training_types (training_type_name)
SELECT 'Running'           WHERE NOT EXISTS (SELECT 1 FROM training_types WHERE training_type_name = 'Running');

ALTER TABLE trainings
DROP CONSTRAINT fk_trainings_trainee,
  ADD CONSTRAINT fk_trainings_trainee
    FOREIGN KEY (trainee_id) REFERENCES trainees(user_id)
    ON DELETE CASCADE;

ALTER TABLE trainee_trainer
DROP CONSTRAINT fk_tt_trainee,
  ADD CONSTRAINT fk_tt_trainee
    FOREIGN KEY (trainee_id) REFERENCES trainees(user_id)
    ON DELETE CASCADE;