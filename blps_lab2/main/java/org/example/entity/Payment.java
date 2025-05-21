package org.example.entity;


import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.entity.enums_status.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//платеж
@Entity
@Getter
@Setter
public class Payment {
    @Id
    @GeneratedValue
    private Long id;

    private Long bookingId;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime paidAt;
}
