package com.uptrix.uptrix_backend.service;

import com.uptrix.uptrix_backend.dto.NotificationDto;
import com.uptrix.uptrix_backend.entity.Notification;
import com.uptrix.uptrix_backend.entity.User;
import com.uptrix.uptrix_backend.repository.NotificationRepository;
import com.uptrix.uptrix_backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NotificationDto createForUser(User user,
                                         String title,
                                         String message,
                                         String type,
                                         String entityType,
                                         Long entityId) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setRelatedEntityType(entityType);
        n.setRelatedEntityId(entityId);
        Notification saved = notificationRepository.save(n);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getMyNotifications() {
        User user = getCurrentUserOrThrow();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getMyUnreadCount() {
        User user = getCurrentUserOrThrow();
        return notificationRepository.countByUserIdAndReadIsFalse(user.getId());
    }

    @Transactional
    public void markRead(Long notificationId) {
        User user = getCurrentUserOrThrow();
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!n.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not allowed");
        }

        n.setRead(true);
        notificationRepository.save(n);
    }

    @Transactional
    public void markAllRead() {
        User user = getCurrentUserOrThrow();
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        for (Notification n : list) {
            if (!n.isRead()) {
                n.setRead(true);
            }
        }
        notificationRepository.saveAll(list);
    }

    // 🔥 THIS IS THE IMPORTANT PART
    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user");
        }

        Object principal = auth.getPrincipal();

        // 1) Your case: principal IS your JPA User entity
        if (principal instanceof User user) {
            return user;
        }

        // 2) Common case: principal is a UserDetails
        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername(); // usually email/username
            return userRepository.findByUsername(username)  // or findByEmail
                    .orElseThrow(() -> new IllegalStateException("User not found by username: " + username));
        }

        // 3) Fallback: principal is just a String (username)
        if (principal instanceof String username) {
            return userRepository.findByUsername(username)  // or findByEmail
                    .orElseThrow(() -> new IllegalStateException("User not found by username: " + username));
        }

        // 4) Anything else: log / throw
        throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
    }

    private NotificationDto toDto(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt());
        dto.setRelatedEntityType(n.getRelatedEntityType());
        dto.setRelatedEntityId(n.getRelatedEntityId());
        return dto;
    }
}
