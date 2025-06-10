package org.example.entity;

import lombok.Data;


@Data
public class AdRequest {
    private String title;
    private String description;
    private String pricePerNight;
}