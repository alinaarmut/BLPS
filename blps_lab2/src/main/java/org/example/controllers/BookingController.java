package org.example.controllers;

/*
 * get - получение статуса заявки /status/{id}
 * подтверждение бронирования - post (хост отправляет статус)
 *  отмена бронирования - post
 */

import lombok.extern.slf4j.Slf4j;
import org.example.entity.primary.Booking;
import org.example.entity.secondary.Offer;
import org.example.repository.primary.BookingRepository;
import org.example.service.BookingService;
import org.example.service.TransactionService.BookingTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/bookings")

public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final BookingTransactionService  bookingTransactionService;

    public BookingController(BookingService bookingService, BookingRepository bookingRepository, BookingTransactionService bookingTransactionService) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.bookingTransactionService = bookingTransactionService;
    }
    // контроллер забронировать (создание бронирования) - post
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/book/")
    public ResponseEntity<Long> createBooking(@RequestBody Map<String, Long> body) {
        try {
            Long adId = body.get("id");
            if (adId == null) {
                return ResponseEntity.badRequest().build();
            }

            log.info("Получен запрос на бронирование по объявлению id = {}", adId);
            Long bookingId = bookingTransactionService.createBookingFromAd(adId);
            return ResponseEntity.ok(bookingId);

        } catch (RuntimeException e) {
            log.error("Ошибка при создании бронирования", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


@PreAuthorize("hasAuthority('USER')")
@GetMapping("/status/{id}")
public ResponseEntity<String> getBookingStatus(@PathVariable("id") Long id) {
    return bookingRepository.findById(id)
            .map(booking -> ResponseEntity.ok(String.valueOf(booking.getBookingStatus())))
            .orElseGet(() -> ResponseEntity.ok("Бронирование не найдено"));
}



    @PostMapping("/cancel/{id}")
    public ResponseEntity<String> cancelBooking(@PathVariable("id") Long id) {
        try {
            Booking booking =  bookingService.cancelBooking(id);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Бронирование отменение");
            responseBody.put("booking", booking);
            return ResponseEntity.ok(responseBody.toString());
        } catch (Exception e) {
            log.error("Ошибка при отмене бронирования", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


@PreAuthorize("hasAuthority('HOST')")
@PostMapping("/accept/{id}")
public ResponseEntity<String> acceptBookingRequest(@PathVariable("id") Long id) {
    log.info("Received request to accept booking with id: {}", id);

    try {
        bookingTransactionService.createOfferFromBookingRequest(id);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Оффер успешно подтвержден");
        responseBody.put("bookingId", id);

        return ResponseEntity.ok(responseBody.toString());
    } catch (IllegalStateException e) {
        log.error("Ошибка бизнес-логики: {}", e.getMessage());
        return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
    } catch (Exception e) {
        log.error("Не получилось создать оффер: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError().body("Внутренняя ошибка сервера");
    }
}
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/{id}/rebook")
    public ResponseEntity<?> retryBooking(@PathVariable("id") Long oldBookingId) {
        try {
            Booking booking = bookingTransactionService.rebook(oldBookingId);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.ok("Повторное бронирование невозможно: " + e.getMessage());
        }
    }


}
