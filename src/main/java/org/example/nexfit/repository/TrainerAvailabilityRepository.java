package org.example.nexfit.repository;

import org.example.nexfit.entity.TrainerAvailability;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerAvailabilityRepository extends MongoRepository<TrainerAvailability, String> {
    
    Optional<TrainerAvailability> findByTrainerId(String trainerId);
}
