package org.example.trainerhub.repository;

import org.example.trainerhub.entity.SavedTrainer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedTrainerRepository extends MongoRepository<SavedTrainer, String> {
    
    Optional<SavedTrainer> findByUserIdAndTrainerId(String userId, String trainerId);
    
    List<SavedTrainer> findByUserIdOrderBySavedAtDesc(String userId);
    
    long countByUserId(String userId);
    
    void deleteByUserIdAndTrainerId(String userId, String trainerId);
}
