package org.example.nexfit.repository;

import org.example.nexfit.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    
    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    List<Notification> findByUserIdAndReadFalse(String userId);
    
    @Query("{ 'userId': ?0 }")
    @Update("{ '$set': { 'read': true, 'readAt': ?1 } }")
    void markAllAsReadForUser(String userId, java.time.LocalDateTime readAt);
    
    long countByUserIdAndReadFalse(String userId);
}
