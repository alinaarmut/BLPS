package org.example.service.TransactionService;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.enums_status.BookingStatus;
import org.example.entity.enums_status.OfferStatus;
import org.example.entity.primary.Booking;
import org.example.entity.secondary.Offer;
import org.example.repository.primary.BookingRepository;
import org.example.repository.secondary.OfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferTransactionService {
    private final EntityManagerFactory primaryEntityManagerFactory;
    @Setter(onMethod_ = {@Autowired, @Qualifier("secondaryEntityManagerFactory")})
    private EntityManagerFactory secondaryEntityManagerFactory;
    private final PlatformTransactionManager transactionManager;
    private final OfferRepository offerRepository;
    private final BookingRepository bookingRepository;
  
    public void acceptOffer(Long offerId) {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        EntityManager primaryEm = primaryEntityManagerFactory.createEntityManager();
        EntityManager secondaryEm = secondaryEntityManagerFactory.createEntityManager();

        try {

            primaryEm.joinTransaction();
            secondaryEm.joinTransaction();
            Offer offer = secondaryEm.find(Offer.class, offerId);
            if (offer == null) {
                throw new IllegalStateException("Оффер не найден");
            }
            if (offer.getStatus() != OfferStatus.SENT) {
               throw new Exception("Оффер должен быть в статусе SENT, чтобы его принять");
            }
            offer.setStatus(OfferStatus.CONFIRMED);
            offer.setConfirmedAt(LocalDateTime.now());

            Long bookingId = offer.getBookingId();
            Booking bookingRequest = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            bookingRequest.setBookingStatus(BookingStatus.CONFIRMED);
//            primaryEm.merge(bookingRequest);

            transactionManager.commit(status);

        } catch (Exception e) {
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
            }
        } finally {
            primaryEm.close();
            secondaryEm.close();
        }
    }


    public void rejectOffer(Long offerId) {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        EntityManager primaryEm = primaryEntityManagerFactory.createEntityManager();
        EntityManager secondaryEm = secondaryEntityManagerFactory.createEntityManager();

        try {

            primaryEm.joinTransaction();
            secondaryEm.joinTransaction();
            Offer offer = secondaryEm.find(Offer.class, offerId);

            if (offer == null) {
                throw new Exception("Оффер не найден");
            }
            offer.setStatus(OfferStatus.REJECTED);


//            secondaryEm.merge(offer);
            log.info("Offer после merge: {}", secondaryEm.find(Offer.class, offerId));
            Long bookingId = offer.getBookingId();
            Booking bookingRequest = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            bookingRequest.setBookingStatus(BookingStatus.REJECTED);
//            primaryEm.merge(bookingRequest);
            log.info("Booking после merge: {}", primaryEm.find(Booking.class, offer.getBookingId()));
            transactionManager.commit(status);
            log.info("Transaction committed");
        } catch (Exception e) {
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
            }
        } finally {
            primaryEm.close();
            secondaryEm.close();
        }
    }
}
