package com.example.gym.entities;

import javax.persistence.*;

@Entity
@Table(name = "training_types")
@org.hibernate.annotations.Immutable
public class TrainingTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_type_name", nullable = false, unique = true, updatable = false)
    private String trainingTypeName;

    protected TrainingTypeEntity() {}

    public TrainingTypeEntity(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }

    public Long getId() { return id; }
    public String getTrainingTypeName() { return trainingTypeName; }
}
