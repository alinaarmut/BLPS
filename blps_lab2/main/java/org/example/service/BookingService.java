package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.Ad;
import org.example.entity.Offer;
import org.example.entity.enums_status.BookingStatus;
import org.example.entity.Booking;
import org.example.repository.AdRepository;
import org.example.repository.BookingRepository;
import org.example.repository.OfferRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.transaction.UserTransaction;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;


@Slf4j
@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final OfferRepository offerRepository;
    private final AdRepository adRepository;
    private final NotificationService notificationService;

    @Resource
    private UserTransaction userTransaction;

    public BookingService(BookingRepository bookingRepository,
                          OfferRepository offerRepository,
                          AdRepository adRepository,
                          NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.offerRepository = offerRepository;
        this.adRepository = adRepository;
        this.notificationService = notificationService;
    }

    public Booking createBooking(Booking request) {
        try {
            log.info("Начало транзакции для создания бронирования");
            userTransaction.begin();

            Ad ad = adRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Объявление не найдено"));
            log.info("Найдено объявление: {}", ad);

            request.setBookingStatus(BookingStatus.PENDING);
            Booking booking = new Booking();
            booking.setAd(ad);
            booking.setTitle(ad.getTitle());
            booking.setDescription(ad.getDescription());
            booking.setPricePerNight(ad.getPricePerNight());
            booking.setBookingStatus(BookingStatus.PENDING);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm:ss", new Locale("ru"));
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
            booking.setTimestamp(now.format(formatter));

            Booking savedBooking = bookingRepository.save(booking);
            notificationService.notifyHost(savedBooking);

            userTransaction.commit();
            log.info("Транзакция успешно зафиксирована");
            return savedBooking;
        } catch (Exception e) {
            try {
                log.error("Ошибка, транзакция будет откатана", e);
                userTransaction.rollback();
            } catch (Exception rollbackEx) {
                log.error("Ошибка при откате транзакции", rollbackEx);
            }
            throw new RuntimeException("Ошибка при создании бронирования", e);
        }
    }

    public boolean cancelBooking(Long id) {
        try {
            userTransaction.begin();

            Booking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));

            if (booking.getBookingStatus() == BookingStatus.PENDING) {
                booking.setBookingStatus(BookingStatus.REJECTED);
                bookingRepository.save(booking);
                userTransaction.commit();
                return true;
            }

            userTransaction.rollback();
            return false;
        } catch (Exception e) {
            try {
                log.error("Ошибка, транзакция будет откатана", e);
                userTransaction.rollback();
            } catch (Exception rollbackEx) {
                log.error("Ошибка при откате транзакции", rollbackEx);
            }
            throw new RuntimeException("Ошибка при отмене бронирования", e);
        }
    }

    public Offer acceptBookingRequest(Long requestId) {
        try {
            userTransaction.begin();
            log.info("Обработка запроса на бронирование с id: {}", requestId);

            Booking bookingRequest = bookingRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));

            if (bookingRequest.getBookingStatus() != BookingStatus.PENDING) {
                log.error("Запрос на бронирование не в статусе PENDING, текущий статус: {}",
                        bookingRequest.getBookingStatus());
                userTransaction.rollback();
                return null;
            }

            bookingRequest.setBookingStatus(BookingStatus.SENT);
            bookingRepository.save(bookingRequest);

            Offer offer = new Offer(bookingRequest);
            offer = offerRepository.save(offer);

            log.info("Оффер создан с id: {}, для запроса на бронирование с id: {}",
                    offer.getId(), offer.getBookingRequest().getId());

            userTransaction.commit();
            return offer;
        } catch (Exception e) {
            try {
                log.error("Ошибка, транзакция будет откатана", e);
                userTransaction.rollback();
            } catch (Exception rollbackEx) {
                log.error("Ошибка при откате транзакции", rollbackEx);
            }
            throw new RuntimeException("Ошибка при принятии запроса на бронирование", e);
        }
    }

    public void cancelExpiredOffers() {
        try {
            userTransaction.begin();

            List<Offer> expiredOffers = offerRepository.findAllBySentAtBefore(
                    LocalDateTime.ofInstant(Instant.now().minusSeconds(86400), ZoneId.systemDefault())
            );

            for (Offer offer : expiredOffers) {
                if (offer.getBookingRequest().getBookingStatus() == BookingStatus.PENDING) {
                    offer.getBookingRequest().setBookingStatus(BookingStatus.REJECTED);
                    offerRepository.save(offer);
                    log.info("Оффер истек, и запрос на бронирование отклонен: {}",
                            offer.getBookingRequest().getId());
                }
            }

            userTransaction.commit();
        } catch (Exception e) {
            try {
                log.error("Ошибка, транзакция будет откатана", e);
                userTransaction.rollback();
            } catch (Exception rollbackEx) {
                log.error("Ошибка при откате транзакции", rollbackEx);
            }
            throw new RuntimeException("Ошибка при отмене просроченных офферов", e);
        }
    }

    public Booking rebook(Long oldBookingId) {
        try {
            userTransaction.begin();

            Booking oldBooking = bookingRepository.findById(oldBookingId)
                    .orElseThrow(() -> new RuntimeException("Предыдущее бронирование не найдено"));

            if (oldBooking.getBookingStatus() != BookingStatus.EXPIRED) {
                userTransaction.rollback();
                throw new IllegalStateException("Только просроченные бронирования могут быть повторно забронированы");
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm:ss", new Locale("ru"));
            LocalDateTime lastAttempt = LocalDateTime.parse(oldBooking.getTimestamp(), formatter);

            if (lastAttempt.plusHours(24).isAfter(LocalDateTime.now())) {
                userTransaction.rollback();
                throw new IllegalStateException("Необходимо подождать 24 часа перед повторной попыткой бронирования");
            }

            Booking newBooking = new Booking();
            newBooking.setTitle(oldBooking.getTitle());
            newBooking.setDescription(oldBooking.getDescription());
            newBooking.setPricePerNight(oldBooking.getPricePerNight());
            newBooking.setBookingStatus(BookingStatus.PENDING);
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
            newBooking.setTimestamp(now.format(formatter));

            Booking saved = bookingRepository.save(newBooking);
            notificationService.notifyHost(saved);

            userTransaction.commit();
            return saved;
        } catch (Exception e) {
            try {
                log.error("Ошибка, транзакция будет откатана", e);
                userTransaction.rollback();
            } catch (Exception rollbackEx) {
                log.error("Ошибка при откате транзакции", rollbackEx);
            }
            throw new RuntimeException("Ошибка при повторном бронировании", e);
        }
    }
}
