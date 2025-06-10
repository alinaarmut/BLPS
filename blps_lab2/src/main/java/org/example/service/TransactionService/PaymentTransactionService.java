package org.example.service.TransactionService;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.enums_status.BookingStatus;
import org.example.entity.enums_status.OfferStatus;
import org.example.entity.enums_status.PaymentStatus;
import org.example.entity.primary.Booking;
import org.example.entity.primary.Payment;
import org.example.entity.secondary.Offer;
import org.example.repository.primary.BookingRepository;
import org.example.repository.primary.PaymentRepository;
import org.example.repository.secondary.OfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final OfferRepository offerRepository;
    private final EntityManagerFactory primaryEntityManagerFactory;
    @Setter(onMethod_ = {@Autowired, @Qualifier("secondaryEntityManagerFactory")})
    private EntityManagerFactory secondaryEntityManagerFactory;
    private final PlatformTransactionManager transactionManager;
    private final BookingRepository bookingRepository;

    public void payForBooking(Long bookingId){

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        EntityManager primaryEm = primaryEntityManagerFactory.createEntityManager();
        EntityManager secondaryEm = secondaryEntityManagerFactory.createEntityManager();

        try {

            primaryEm.joinTransaction();
            secondaryEm.joinTransaction();

            Offer offer = offerRepository.findByBookingId(bookingId);
        if (offer == null) {
            throw new RuntimeException("Оффер не найден");
        }
        if (offer.getStatus() != OfferStatus.CONFIRMED) {
            throw new RuntimeException("Оффер должен быть в статусе CONFIRMED для оплаты");
        }
            LocalDateTime confirmedAt = offer.getConfirmedAt();
            if (confirmedAt == null) {
                throw new RuntimeException("У оффера отсутствует время подтверждения");
            }

            Duration duration = Duration.between(confirmedAt, LocalDateTime.now());
        if (duration.toMinutes() > 15) {
            offer.setStatus(OfferStatus.EXPIRED);
            secondaryEm.merge(offer);
            throw new RuntimeException("Срок действия оффера истек. Оплата невозможна.");

        }
        offer.setStatus(OfferStatus.PAYED);
        secondaryEm.merge(offer);


        Long bookId = offer.getBookingId();
        Booking booking = bookingRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setBookingStatus(BookingStatus.PAYED);
        primaryEm.merge(booking);
        primaryEm.persist(createPayment(booking));
            transactionManager.commit(status);
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

    public Payment createPayment(Booking booking){
        Payment payment = new Payment();
        payment.setBookingId(booking.getId());
        payment.setAmount(BigDecimal.valueOf(booking.getPricePerNight()));
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        return payment;
    }

}
