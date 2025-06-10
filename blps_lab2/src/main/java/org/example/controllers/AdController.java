package org.example.controllers;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.primary.Ad;
import org.example.entity.AdRequest;
import org.example.repository.primary.AdRepository;
import org.example.service.AdService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/ad")
public class AdController {
    private final AdRepository adRepository;
    private  final AdService advertisementService;

    public AdController(AdRepository adRepository, AdService advertisementService) {
        this.adRepository = adRepository;
        this.advertisementService = advertisementService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Ad>> getAllAds() {
        List<Ad> ads = adRepository.findAll();
        if (ads.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ads);
    }

    @PreAuthorize("hasAuthority('HOST')")
    @PostMapping("/ads")
    public ResponseEntity<Ad> createAd(@RequestBody AdRequest request) {
        log.info("Received request to create an ad with title: {}, description: {}, pricePerNight: {}",
                request.getTitle(), request.getDescription(), request.getPricePerNight());

        try {
            Ad ad = advertisementService.createAd( request.getTitle(), request.getDescription(), Double.valueOf(request.getPricePerNight()));
            log.info("Объявление успешно создано: {}", ad);

            return ResponseEntity.status(HttpStatus.CREATED).body(ad);
        } catch (Exception e) {
            log.error("Ошибка при создании объявления: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

}
