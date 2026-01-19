package org.example.trainerhub.repository;

import org.example.trainerhub.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    
    Page<Review> findByTrainerId(String trainerId, Pageable pageable);

    List<Review> findByTrainerId(String trainerId);
}
