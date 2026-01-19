package org.example.trainerhub.repository;

import org.example.trainerhub.entity.SkippedTrainer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkippedTrainerRepository extends MongoRepository<SkippedTrainer, String> {
    
    Optional<SkippedTrainer> findByUserIdAndTrainerId(String userId, String trainerId);
}
