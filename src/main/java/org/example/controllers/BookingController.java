package org.example.controllers;

/*
 * get - получение статуса заявки /status/{id}
 * подтверждение бронирования - post (хост отправляет статус)
 *  отмена бронирования - post
 */

import lombok.extern.slf4j.Slf4j;
import org.example.entity.Ad;
import org.example.entity.Booking;
import org.example.entity.Offer;
import org.example.repository.BookingRepository;
import org.example.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/bookings")

public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    public BookingController(BookingService bookingService, BookingRepository bookingRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
    } //так лучше, чем @autowired или @allargsConstructor
    // контроллер забронировать (создание бронирования) - post

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/book/")
    public ResponseEntity<Booking> createBooking(@RequestBody Booking request) {
        log.info("Received booking request: {}", request);
        Booking booking = bookingService.createBooking(request);
        log.info("Бронирование успешно создано: {}", booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
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
        log.info("Received request to cancel booking with ID: {}", id);
        boolean canceled = bookingService.cancelBooking(id);
        if (canceled) {
            return ResponseEntity.ok("Бронирование успешно отменено");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to cancel booking");
        }
    }
    @PreAuthorize("hasAuthority('HOST')")
    @PostMapping("/accept/{id}")
    public ResponseEntity<Offer> acceptBookingRequest(@PathVariable("id") Long id) {
        log.info("Received request to accept booking with id: {}", id);

        Offer offer = bookingService.acceptBookingRequest(id);

        if (offer != null) {
            log.info("Оффер успешно создан: {}", offer);
            return ResponseEntity.ok(offer);
        } else {
            log.error("Не получилось создать оффер для бронирования с id: {}", id);
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/{id}/rebook")
    public ResponseEntity<?> retryBooking(@PathVariable("id") Long oldBookingId) {
        try {
            Booking booking = bookingService.rebook(oldBookingId);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.ok("Повторное бронирование невозможно: " + e.getMessage());
        }
    }


}
