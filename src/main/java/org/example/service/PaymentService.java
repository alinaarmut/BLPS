package org.example.service;

import jakarta.transaction.Transactional;
import org.example.entity.Booking;
import org.example.entity.Offer;
import org.example.entity.Payment;
import org.example.entity.enums_status.BookingStatus;
import org.example.entity.enums_status.OfferStatus;
import org.example.entity.enums_status.PaymentStatus;
import org.example.repository.BookingRepository;
import org.example.repository.OfferRepository;
import org.example.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final OfferRepository offerRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;


    public PaymentService(OfferRepository offerRepository, BookingRepository bookingRepository
    , PaymentRepository paymentRepository) {
        this.offerRepository = offerRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
    }
    @Transactional
    public ResponseEntity<?> payForBooking(Long bookingId) {
        Offer offer = offerRepository.findByBookingRequestId(bookingId);

        if (offer == null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Оффер не найден для бронирования с id " + bookingId);
        }

        if (offer.getStatus() != OfferStatus.CONFIRMED) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Offer is not confirmed");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime confirmedAt = offer.getConfirmedAt();

        if (confirmedAt == null) {

            return ResponseEntity.status(HttpStatus.OK)
                    .body("Confirmed time is missing");
        }

        Duration duration = Duration.between(confirmedAt, now);
        if (duration.toMinutes() > 15) {
            // если прошло больше 15 минут, статус меняется на EXPIRED
            offer.setStatus(OfferStatus.EXPIRED);
            offerRepository.save(offer);
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Срок действия истек. Оплата невозможна.");
        }

        offer.setStatus(OfferStatus.PAYED);
        offerRepository.save(offer);

        Booking booking = offer.getBookingRequest();
        booking.setBookingStatus(BookingStatus.PAYED);
        bookingRepository.save(booking);


        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setAmount(BigDecimal.valueOf(booking.getPricePerNight()));
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());

        paymentRepository.save(payment);

        return ResponseEntity.ok(payment);
    }
}