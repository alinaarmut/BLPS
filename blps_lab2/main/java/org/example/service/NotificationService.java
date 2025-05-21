package org.example.service;


import lombok.extern.slf4j.Slf4j;
import org.example.entity.Booking;
import org.example.entity.Notification;
import org.example.entity.User;
import org.example.entity.enums_status.UserRole;
import org.example.repository.NotificationRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.transaction.UserTransaction;
import java.util.List;
@Slf4j
@Service
public class NotificationService {

@Resource
private UserTransaction userTransaction;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void notifyHost(Booking request) {
        try {
            log.info("Начало транзакции Narayana для уведомления хоста");
            userTransaction.begin();

            User host = userRepository.findByRoles_RoleName(UserRole.HOST)
                    .orElseThrow(() -> new RuntimeException("Host not found"));

            System.out.println("📨 Уведомление ХОСТУ: поступила заявка на бронирование!");
            System.out.println("🛏 Название: " + request.getTitle());
            System.out.println("💬 Описание: " + request.getDescription());

            Notification notification = new Notification(System.currentTimeMillis(), host, request);
            notificationRepository.save(notification);

            userTransaction.commit();
            log.info("Уведомление успешно сохранено и транзакция зафиксирована");

        } catch (Exception e) {
            try {
                log.error("Ошибка при уведомлении, откатываем транзакцию", e);
                userTransaction.rollback();
            } catch (Exception rollbackEx) {
                log.error("Ошибка при откате транзакции", rollbackEx);
            }
            throw new RuntimeException("Ошибка при отправке уведомления", e);
        }
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
