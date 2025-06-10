package org.example.repository.secondary;

import org.example.entity.secondary.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findAllBySentAtBefore(LocalDateTime sentAt);
    Offer findByBookingId(Long bookingId);

}
