package org.example.nexfit.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexfit.entity.Notification;
import org.example.nexfit.exception.BusinessException;
import org.example.nexfit.exception.ResourceNotFoundException;
import org.example.nexfit.model.response.PageResponse;
import org.example.nexfit.repository.NotificationRepository;
import org.example.nexfit.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    @Override
    public PageResponse<Notification> getUserNotifications(String userId, Pageable pageable) {
        Page<Notification> notificationsPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return PageResponse.<Notification>builder()
                .data(notificationsPage.getContent())
                .pagination(PageResponse.PaginationInfo.builder()
                        .page(notificationsPage.getNumber())
                        .limit(notificationsPage.getSize())
                        .total(notificationsPage.getTotalElements())
                        .totalPages(notificationsPage.getTotalPages())
                        .hasNext(notificationsPage.hasNext())
                        .hasPrevious(notificationsPage.hasPrevious())
                        .build())
                .build();
    }
    
    @Override
    public void markAsRead(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("You don't have permission to mark this notification as read");
        }
        
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
    
    @Override
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsReadForUser(userId, LocalDateTime.now());
    }
    
    @Override
    public void deleteNotification(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("You don't have permission to delete this notification");
        }
        
        notificationRepository.deleteById(notificationId);
    }
    
    @Override
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
}
