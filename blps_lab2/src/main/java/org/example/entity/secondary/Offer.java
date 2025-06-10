package org.example.entity.secondary;


// оффер от хоста


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.example.entity.primary.Booking;
import org.example.entity.enums_status.OfferStatus;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Entity
@Table(name = "offer")
@Data
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Enumerated(EnumType.STRING)
    private OfferStatus status;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy, HH:mm:ss", locale = "en")
    private LocalDateTime sentAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy, HH:mm:ss", locale = "en")
    private LocalDateTime confirmedAt;


    public Offer(Booking booking) {
        this.bookingId = booking.getId();
        this.status = OfferStatus.SENT;
        this.sentAt = LocalDateTime.now();

    }

    public Offer() {}


}
