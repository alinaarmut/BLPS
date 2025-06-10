package org.example.controllers;

import org.example.entity.secondary.Notification;
import org.example.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    // в будущем сюда подавать хоста чтобы проверить что у определенного хоста свои уведомления

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<List<Notification>> getNotification() {
        List<Notification> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

}
