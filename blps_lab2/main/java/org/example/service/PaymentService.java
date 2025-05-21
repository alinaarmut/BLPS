package org.example.service;

import javax.annotation.Resource;

import javax.transaction.UserTransaction;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
public class PaymentService {

    private final OfferRepository offerRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Resource
    private UserTransaction userTransaction;

    public PaymentService(OfferRepository offerRepository,
                          BookingRepository bookingRepository,
                          PaymentRepository paymentRepository) {
        this.offerRepository = offerRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
    }

    public ResponseEntity<?> payForBooking(Long bookingId) {
        try {

            userTransaction.begin();

            Offer offer = offerRepository.findByBookingRequestId(bookingId);
            if (offer == null) {
                userTransaction.rollback();
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Оффер не найден для бронирования с id " + bookingId);
            }

            if (offer.getStatus() != OfferStatus.CONFIRMED) {
                userTransaction.rollback();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Оффер должен быть в статусе CONFIRMED для оплаты");
            }

            LocalDateTime confirmedAt = offer.getConfirmedAt();
            if (confirmedAt == null) {
                userTransaction.rollback();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("У оффера отсутствует время подтверждения");
            }

            Duration duration = Duration.between(confirmedAt, LocalDateTime.now());
            if (duration.toMinutes() > 15) {
                offer.setStatus(OfferStatus.EXPIRED);
                offerRepository.save(offer);
                userTransaction.commit(); // фиксируем обновление статуса оффера
                return ResponseEntity.status(HttpStatus.GONE)
                        .body("Срок действия оффера истек. Оплата невозможна.");
            }

            // Обновляем статус оффера
            offer.setStatus(OfferStatus.PAYED);
            offerRepository.save(offer);

            // Обновляем статус бронирования
            Booking booking = offer.getBookingRequest();
            booking.setBookingStatus(BookingStatus.PAYED);
            bookingRepository.save(booking);

            // Создаем платеж
            Payment payment = new Payment();
            payment.setBookingId(bookingId);
            payment.setAmount(BigDecimal.valueOf(booking.getPricePerNight()));
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            userTransaction.commit();

            return ResponseEntity.ok(payment);

        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (Exception rollbackEx) {
                log.error("Ошибка при откате транзакции", rollbackEx);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при обработке платежа");
        }
    }
}