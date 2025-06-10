package org.example.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.primary.Ad;
import org.example.entity.secondary.Offer;
import org.example.entity.enums_status.BookingStatus;
import org.example.entity.primary.Booking;
import org.example.repository.primary.AdRepository;
import org.example.repository.primary.BookingRepository;
import org.example.repository.secondary.OfferRepository;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.transaction.UserTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final OfferRepository offerRepository;
    private final BookingRepository bookingRepository;
    @Resource
    private UserTransaction userTransaction;
    private final EntityManagerFactory primaryEntityManagerFactory;
    private final PlatformTransactionManager transactionManager;


    public Booking cancelBooking(Long id) {
        EntityManager primaryEm = primaryEntityManagerFactory.createEntityManager();
        var status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            primaryEm.joinTransaction();

            Booking booking = primaryEm.find(Booking.class, id);
            if (booking == null) {
                throw  new IllegalStateException("Объявление не найдено");
            }
            if (booking.getBookingStatus() == BookingStatus.PENDING) {
                booking.setBookingStatus(BookingStatus.REJECTED);
                primaryEm.merge(booking);
                transactionManager.commit(status);
            }
            return booking;
        } catch (Exception e) {
            try {
                log.error("Ошибка, транзакция будет откатана", e);
                transactionManager.rollback(status);
            } catch (Exception rollbackEx) {
                log.error("Ошибка при откате транзакции", rollbackEx);
            }
            throw new RuntimeException("Ошибка при отмене бронирования", e);
        }

    }


    public void cancelExpiredOffers() {
        EntityManager primaryEm = primaryEntityManagerFactory.createEntityManager();
        var status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            primaryEm.joinTransaction();
            List<Offer> expiredOffers = offerRepository.findAllBySentAtBefore(
                    LocalDateTime.ofInstant(Instant.now().minusSeconds(86400), ZoneId.systemDefault())
            );

            for (Offer offer : expiredOffers) {
                Optional<Booking> optionalBooking = bookingRepository.findById(offer.getBookingId());
                if (optionalBooking.isPresent()) {
                    Booking booking = optionalBooking.get();
                    if (booking.getBookingStatus() == BookingStatus.PENDING) {
                        booking.setBookingStatus(BookingStatus.REJECTED);
                        primaryEm.merge(booking);
                        log.info("Оффер истек, и запрос на бронирование отклонен: {}", booking.getId());
                    }
                } else {
                    log.warn("Бронирование с id={} не найдено для оффера id={}", offer.getBookingId(), offer.getId());
                }
            }


            transactionManager.commit(status);
        } catch (Exception e) {
            try {
                log.error("Ошибка, транзакция будет откатана", e);
                transactionManager.rollback(status);
            } catch (Exception rollbackEx) {
                log.error("Ошибка при откате транзакции", rollbackEx);
            }
            throw new RuntimeException("Ошибка при отмене просроченных офферов", e);
        }
    }


}
