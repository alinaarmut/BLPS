package org.example.service;

import javax.annotation.Resource;
import javax.transaction.UserTransaction;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.Booking;
import org.example.entity.Offer;
import org.example.entity.enums_status.BookingStatus;
import org.example.entity.enums_status.OfferStatus;
import org.example.repository.BookingRepository;
import org.example.repository.OfferRepository;

import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
@Slf4j
@Service
public class OfferService {
    private final OfferRepository offerRepository;
    private final BookingRepository bookingRepository;

    @Resource
    private UserTransaction userTransaction;

    public OfferService(OfferRepository offerRepository, BookingRepository bookingRepository) {
        this.offerRepository = offerRepository;
        this.bookingRepository = bookingRepository;
    }

    public String acceptOffer(Long offerId) {
        try {
            log.info("Попытка принять оффер с id: {}", offerId);
            userTransaction.begin();

            Offer offer = offerRepository.findById(offerId).orElse(null);
            if (offer == null) {
                log.warn("Оффер с id {} не найден", offerId);
                userTransaction.rollback();
                return "Оффер не найден";
            }

            if (offer.getStatus() != OfferStatus.SENT) {
                log.warn("Оффер с id {} не может быть принят. Текущий статус: {}", offerId, offer.getStatus());
                userTransaction.rollback();
                return "Offer cannot be accepted, current status: " + offer.getStatus();
            }

            offer.setStatus(OfferStatus.CONFIRMED);
            offer.setConfirmedAt(LocalDateTime.now());
            offerRepository.save(offer);

            Booking bookingRequest = offer.getBookingRequest();
            bookingRequest.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(bookingRequest);

            userTransaction.commit();
            log.info("Оффер с id {} подтверждён и статус бронирования обновлён", offerId);
            return "Оффер принят, статус бронирования обновлён на CONFIRMED";
        } catch (Exception e) {
            try {
                log.error("Ошибка при принятии оффера с id {}: {}", offerId, e.getMessage());
                userTransaction.rollback();
            } catch (Exception rollbackEx) {
                log.error("Ошибка при откате транзакции: {}", rollbackEx.getMessage());
            }
            return "Произошла ошибка при принятии оффера";
        }
    }

    public String rejectOffer(Long offerId) {
        try {
            log.info("Попытка отклонить оффер с id: {}", offerId);
            userTransaction.begin();

            Offer offer = offerRepository.findById(offerId).orElse(null);
            if (offer == null) {
                log.warn("Оффер с id {} не найден", offerId);
                userTransaction.rollback();
                return "Оффер не найден";
            }

            offer.setStatus(OfferStatus.REJECTED);
            offerRepository.save(offer);

            Booking bookingRequest = offer.getBookingRequest();
            bookingRequest.setBookingStatus(BookingStatus.REJECTED);
            bookingRepository.save(bookingRequest);

            userTransaction.commit();
            log.info("Оффер с id {} отклонён и статус бронирования обновлён", offerId);
            return "Оффер отклонён, статус бронирования обновлён на REJECTED";
        } catch (Exception e) {
            try {
                log.error("Ошибка при отклонении оффера с id {}: {}", offerId, e.getMessage());
                userTransaction.rollback();
            } catch (Exception rollbackEx) {
                log.error("Ошибка при откате транзакции: {}", rollbackEx.getMessage());
            }
            return "Произошла ошибка при отклонении оффера";
        }
    }
}
