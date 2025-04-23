package org.example.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Ad;
import org.example.repository.AdRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdService {

    private final AdRepository advertisementRepository;

    public AdService(AdRepository advertisementRepository) {
        this.advertisementRepository = advertisementRepository;
    }

    @Transactional
    public Ad createAd(String title, String description, Double pricePerNight) {
        try {
            log.info("Создание объявления с параметрами: title={}, description={}, pricePerNight={}", title, description, pricePerNight);


            Ad ad = new Ad();
            ad.setTitle(title);
            ad.setDescription(description);
            ad.setPricePerNight(pricePerNight);


            ad = advertisementRepository.save(ad);

            log.info("Объявление успешно создано с id: {}", ad.getId());
            return ad;
        } catch (Exception e) {
            log.error("Ошибка при создании объявления: ", e);
            throw new RuntimeException("Ошибка при создании объявления", e);
        }
    }

}
