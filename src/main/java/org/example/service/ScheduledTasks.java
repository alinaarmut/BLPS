package org.example.service;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTasks {
    private final BookingService bookingService;

    public ScheduledTasks(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Scheduled(fixedRate = 86400000) // Каждые 24 часа
    public void checkExpiredOffers() {
        bookingService.cancelExpiredOffers();
    }
}
