package org.example.nexfit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.nexfit.entity.Notification;
import org.example.nexfit.entity.User;
import org.example.nexfit.model.response.PageResponse;
import org.example.nexfit.repository.UserRepository;
import org.example.nexfit.service.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management APIs")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    
    @GetMapping
    @Operation(summary = "Get user notifications")
    public ResponseEntity<PageResponse<Notification>> getUserNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(notificationService.getUserNotifications(user.getId(), PageRequest.of(page, limit)));
    }
    
    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }
    
    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Map<String, String>> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        notificationService.deleteNotification(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Notification deleted"));
    }
    
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        long count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
}
