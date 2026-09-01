package com.example.training.repository;

import com.example.training.document.TrainerMonthlySummaryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerMonthlySummaryRepository
        extends MongoRepository<TrainerMonthlySummaryDocument, String>
{
    Optional<TrainerMonthlySummaryDocument> findByUsername(String username);
}
