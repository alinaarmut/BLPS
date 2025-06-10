package org.example.entity.primary;


import jakarta.persistence.*;
import lombok.*;



@Data
@Entity
@Table(name = "advertaisment", schema = "s367826")
@NoArgsConstructor

public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ad_seq")
    @SequenceGenerator(name = "ad_seq", sequenceName = "advertaisment_id_seq", allocationSize = 1)
    private Long id;
    @Column(name = "title")
    private String title;
    @Column(name = "description")
    private String description;
    @Column(name = "price_per_night")
    private Double pricePerNight;

}
