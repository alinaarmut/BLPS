package org.example.service;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.primary.Ad;
import org.example.repository.primary.AdRepository;
import org.springframework.stereotype.Service;



import jakarta.annotation.Resource;
import jakarta.transaction.UserTransaction;
import org.springframework.transaction.PlatformTransactionManager;


@Service
@Slf4j
@RequiredArgsConstructor
public class AdService {

    @Resource
    private UserTransaction userTransaction;
    private final EntityManagerFactory primaryEntityManagerFactory;
    private final PlatformTransactionManager transactionManager;

public Ad createAd(String title, String description, Double pricePerNight) {
    EntityManager primaryEm = primaryEntityManagerFactory.createEntityManager();
    var status = transactionManager.getTransaction(null);
    try {
        log.info("Начинаем транзакцию");
        log.info("Транзакция начата, статус: {}", userTransaction.getStatus());


       primaryEm.joinTransaction();

        log.info("Создание объявления с параметрами: title={}, description={}, pricePerNight={}", title, description, pricePerNight);

        Ad ad = new Ad();
        ad.setTitle(title);
        ad.setDescription(description);
        ad.setPricePerNight(pricePerNight);

        primaryEm.persist(ad);

        transactionManager.commit(status);

        log.info("Транзакция успешно зафиксирована, id: {}", ad.getId());

        return ad;

    } catch (Exception e) {
        log.error("Ошибка при создании объявления: {}", e.getMessage(), e);
        try {
            transactionManager.rollback(status);
        } catch (Exception rollbackEx) {
            log.error("Ошибка при откате транзакции", rollbackEx);
        }
        throw new RuntimeException("Ошибка при создании объявления", e);
    } finally {
        primaryEm.close();

    }
}

}
