package com.abi.coding_tracker.notification.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.exception.ResourceNotFoundException;
import com.abi.coding_tracker.notification.dto.NotificationResponse;
import com.abi.coding_tracker.notification.entity.Notification;
import com.abi.coding_tracker.notification.repository.NotificationRepository;
import com.abi.coding_tracker.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public NotificationController(UserRepository userRepository, NotificationRepository notificationRepository){
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(Authentication authentication){
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(()->new ResourceNotFoundException("User Not Found"));

        List<NotificationResponse> responses = notificationRepository.findTop20ByUserOrderByCreatedAtDesc(user)
                                    .stream().map(this::mapToResponse).collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication authentication){
        Notification notification = notificationRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Notification Not found"));

        if(!notification.getUser().getEmail().equals(authentication.getName())){
            throw new ResourceNotFoundException("Notification Not found");
        }

        notification.setRead(true);
        notificationRepository.save(notification);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id, Authentication authentication){
        Notification notification = notificationRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Notification not found"));

        if(!notification.getUser().getEmail().equals(authentication.getName())){
            throw new ResourceNotFoundException("Notification Not Found");
        }

        notificationRepository.delete(notification);
        return ResponseEntity.ok().build();
    }

    private NotificationResponse mapToResponse(Notification n){
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType().name())
                .subject(n.getSubject())
                .message(n.getMessage())
                .status(n.getStatus().name())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }

}
