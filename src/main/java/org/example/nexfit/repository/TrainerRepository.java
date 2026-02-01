package org.example.nexfit.repository;

import org.example.nexfit.entity.Trainer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerRepository extends MongoRepository<Trainer, String> {
    
    Optional<Trainer> findByEmail(String email);
}
