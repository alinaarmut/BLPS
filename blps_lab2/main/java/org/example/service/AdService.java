package org.example.service;


import lombok.extern.slf4j.Slf4j;
import org.example.entity.Ad;
import org.example.repository.AdRepository;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import javax.transaction.UserTransaction;

@Service
@Slf4j
public class AdService {

@Resource
private UserTransaction userTransaction;

    private final AdRepository advertisementRepository;

    public AdService(AdRepository advertisementRepository) {
        this.advertisementRepository = advertisementRepository;
    }

    public Ad createAd(String title, String description, Double pricePerNight) {
        try {
            log.info("Начало транзакции Narayana для создания объявления");
            userTransaction.begin();

            log.info("Создание объявления с параметрами: title={}, description={}, pricePerNight={}", title, description, pricePerNight);

            Ad ad = new Ad();
            ad.setTitle(title);
            ad.setDescription(description);
            ad.setPricePerNight(pricePerNight);

            Ad savedAd = advertisementRepository.save(ad);

            log.info("Сохранено объявление с id: {}", savedAd.getId());

            userTransaction.commit();
            log.info("Транзакция успешно зафиксирована");

            return savedAd;
        } catch (Exception e) {
            try {
                log.error("Ошибка, транзакция будет откатана", e);
                userTransaction.rollback();
            } catch (Exception rollbackEx) {
                log.error("Ошибка при откате транзакции", rollbackEx);
            }
            throw new RuntimeException("Ошибка при создании объявления", e);
        }
    }

}
