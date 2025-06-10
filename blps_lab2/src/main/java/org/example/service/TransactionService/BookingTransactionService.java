package org.example.service.TransactionService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.enums_status.BookingStatus;
import org.example.entity.enums_status.OfferStatus;
import org.example.entity.primary.Ad;
import org.example.entity.primary.Booking;
import org.example.entity.primary.User;
import org.example.entity.secondary.Notification;

import org.example.entity.enums_status.UserRole;
import org.example.entity.secondary.Offer;
import org.example.repository.primary.AdRepository;
import org.example.repository.primary.BookingRepository;
import org.example.repository.primary.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.*;
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingTransactionService {

    private final EntityManagerFactory primaryEntityManagerFactory;
    @Setter(onMethod_ = {@Autowired, @Qualifier("secondaryEntityManagerFactory")})
    private EntityManagerFactory secondaryEntityManagerFactory;
    private final PlatformTransactionManager transactionManager;
    private final AdRepository adRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;


    // транзакция 1
    public Long createBookingFromAd(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Объявление не найдено: id = " + adId));

        Booking booking = new Booking();
        booking.setAd(ad);
        booking.setTitle(ad.getTitle());
        booking.setDescription(ad.getDescription());
        booking.setPricePerNight(ad.getPricePerNight());
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setTimestamp(LocalDateTime.now());
        booking.setHostId(2L);

        return createBookings(booking);
    }

    public Long createBookings(Booking booking) {
        return createBooking(booking);
    }



    public Long createBooking(Booking booking) {
        var status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        EntityManager primaryEm = primaryEntityManagerFactory.createEntityManager();
        EntityManager secondaryEm = secondaryEntityManagerFactory.createEntityManager();


        try {
            primaryEm.joinTransaction();
            secondaryEm.joinTransaction();


            primaryEm.persist(booking);
            secondaryEm.flush();

            Notification notification = createNotificationForHost(booking);
            secondaryEm.persist(notification);

            transactionManager.commit(status);
            return booking.getId();
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        } finally {
            primaryEm.close();
            secondaryEm.close();
        }
    }

    private Notification createNotificationForHost(Booking booking) {
        User host = userRepository.findByRoles_RoleName(UserRole.HOST)
                .orElseThrow(() -> new RuntimeException("Host not found"));

        System.out.println("📨 Уведомление ХОСТУ: поступила заявка на бронирование!");
        System.out.println("🛏 Название: " + booking.getTitle());
        System.out.println("💬 Описание: " + booking.getDescription());

        return new Notification(
                host.getId(),
                booking.getId()
        );
    }


//    транзакция 2

public Long createOffer(Offer offer) {
    TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
    EntityManager primaryEm = primaryEntityManagerFactory.createEntityManager();
    EntityManager secondaryEm = secondaryEntityManagerFactory.createEntityManager();


    try {
        primaryEm.joinTransaction();
        secondaryEm.joinTransaction();
        Booking booking = primaryEm.find(Booking.class, offer.getBookingId());
        if (booking == null || booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Бронирование не найдено или статус не PENDING");
        }
        log.info("Persisting offer {} into DB: {}", offer.getBookingId(), secondaryEm.createNativeQuery("SELECT current_database()").getSingleResult());

        secondaryEm.persist(offer);
        secondaryEm.flush();

        booking.setBookingStatus(BookingStatus.SENT);
        primaryEm.merge(booking);


        log.info("Offer сохранён с id = {}", offer.getId());

        transactionManager.commit(status);

        return offer.getId();
    } catch (Exception e) {
        if (!status.isCompleted()) {
            transactionManager.rollback(status);
        }
        throw e;
    } finally {
        primaryEm.close();
        secondaryEm.close();
    }
}


    public void createOfferFromBookingRequest(Long bookingId) {
        bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));
        Offer offer = new Offer();
        offer.setBookingId(bookingId);
        offer.setStatus(OfferStatus.SENT);
        offer.setSentAt(LocalDateTime.now());

        createOffer(offer);
    }


    // транзакция 3

    public Booking rebook(Long oldBookingId) {
        Booking oldBooking = bookingRepository.findById(oldBookingId)
                .orElseThrow(() -> new RuntimeException("Предыдущее бронирование не найдено"));

        if (oldBooking.getBookingStatus() != BookingStatus.EXPIRED) {
            throw new IllegalStateException("Только просроченные бронирования могут быть повторно забронированы");
        }

        LocalDateTime lastAttempt = oldBooking.getTimestamp();

        if (lastAttempt.plusHours(24).isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Необходимо подождать 24 часа перед повторной попыткой бронирования");
        }

        Booking newBooking = new Booking();
        newBooking.setTitle(oldBooking.getTitle());
        newBooking.setDescription(oldBooking.getDescription());
        newBooking.setPricePerNight(oldBooking.getPricePerNight());
        newBooking.setBookingStatus(BookingStatus.PENDING);
        newBooking.setTimestamp(LocalDateTime.now());



        createBooking(newBooking);
        return newBooking;
    }

}
