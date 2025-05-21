package org.example.entity;


import javax.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "notification")

public class Notification {
    @Id
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Booking bookingRequest;
    public Notification(Long id, User user, Booking bookingRequest) {
        this.id = id;
        this.user = user;
        this.bookingRequest = bookingRequest;
    }

    public Notification() {

    }
}
