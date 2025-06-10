package org.example.service;


import lombok.extern.slf4j.Slf4j;
import org.example.entity.primary.Booking;
import org.example.entity.secondary.Notification;
import org.example.entity.primary.User;
import org.example.entity.enums_status.UserRole;
import org.example.repository.secondary.NotificationRepository;
import org.example.repository.primary.UserRepository;
import org.springframework.stereotype.Service;


import jakarta.annotation.Resource;
import jakarta.transaction.UserTransaction;
import java.util.List;
@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
