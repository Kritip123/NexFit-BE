package org.example.nexfit.repository;

import org.example.nexfit.entity.TrainingCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingCategoryRepository extends MongoRepository<TrainingCategory, String> {

    List<TrainingCategory> findByActiveTrueOrderByDisplayOrderAsc();

    Optional<TrainingCategory> findByName(String name);

    boolean existsByName(String name);
}
