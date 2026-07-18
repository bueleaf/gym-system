INSERT INTO training_types (training_type_name)
VALUES ('Yoga')
    ON CONFLICT (training_type_name) DO NOTHING;

INSERT INTO training_types (training_type_name)
VALUES ('Cardio')
    ON CONFLICT (training_type_name) DO NOTHING;

INSERT INTO training_types (training_type_name)
VALUES ('Strength Training')
    ON CONFLICT (training_type_name) DO NOTHING;

INSERT INTO training_types (training_type_name)
VALUES ('Pilates')
    ON CONFLICT (training_type_name) DO NOTHING;

INSERT INTO training_types (training_type_name)
VALUES ('CrossFit')
    ON CONFLICT (training_type_name) DO NOTHING;

INSERT INTO training_types (training_type_name)
VALUES ('Boxing')
    ON CONFLICT (training_type_name) DO NOTHING;

INSERT INTO training_types (training_type_name)
VALUES ('Swimming')
    ON CONFLICT (training_type_name) DO NOTHING;

INSERT INTO training_types (training_type_name)
VALUES ('Running')
    ON CONFLICT (training_type_name) DO NOTHING;