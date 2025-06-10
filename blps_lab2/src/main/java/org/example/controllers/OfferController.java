package org.example.controllers;


import lombok.extern.slf4j.Slf4j;
import org.example.service.TransactionService.OfferTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/offer")
public class OfferController {

    private final OfferTransactionService offerTransactionService;

    public OfferController(OfferTransactionService offerTransactionService) {
        this.offerTransactionService = offerTransactionService;
    }


    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/accept/{offerId}")
    public ResponseEntity<?> acceptOffer(@PathVariable("offerId") Long offerId) {
        try {
            offerTransactionService.acceptOffer(offerId);
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Оффер успешно принять");
            return ResponseEntity.ok(res.toString());
        } catch (Exception e) {
            if (e.getMessage().contains("Оффер не найден")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if ((e.getMessage().contains("SENT"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            } else {
                log.error("ОШИБКА В ПРИНЯТИИ ОФФЕРА", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

            }
        }
    }

    // отклонение оффера после подтверждения хоста
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/reject/{offerId}")
    public ResponseEntity<String> rejectOffer(@PathVariable Long offerId) {
        try {
            offerTransactionService.rejectOffer(offerId);
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Оффер отозван");
            return ResponseEntity.ok(res.toString());
        } catch (Exception e) {
            if (e.getMessage().contains("Оффер не найден")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else {
                log.error("ОШИБКА В ОТКАЗЕ ОФФЕРА", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

            }
        }
    }
}
