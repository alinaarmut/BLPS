package org.example.service;


import jakarta.transaction.Transactional;
import org.example.entity.Booking;
import org.example.entity.Notification;
import org.example.entity.User;
import org.example.entity.enums_status.UserRole;
import org.example.repository.NotificationRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void notifyHost(Booking request) {
        User host = userRepository.findByRoles_RoleName(UserRole.HOST).orElseThrow(() -> new RuntimeException("Host not found"));
        System.out.println("📨 Уведомление ХОСТУ: поступила заявка на бронирование!");
        System.out.println("🛏 Название: " + request.getTitle());
        System.out.println("💬 Описание: " + request.getDescription());

        Notification notification = new Notification(System.currentTimeMillis(), host, request);
        notificationRepository.save(notification);
    }



    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
