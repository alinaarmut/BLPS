package org.example.entity;


import jakarta.persistence.*;
import lombok.*;



@Data
@Entity
@Table(name = "advertaisment")
@NoArgsConstructor
@AllArgsConstructor
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title")
    private String title;
    @Column(name = "description")
    private String description;
    @Column(name = "pricePerNight")
    private Double pricePerNight;

}
