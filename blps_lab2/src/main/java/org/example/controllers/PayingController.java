package org.example.controllers;


import lombok.extern.slf4j.Slf4j;
import org.example.service.TransactionService.PaymentTransactionService;
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
@RequestMapping("/paying")
public class PayingController {

    // post - pay метод - гость оплачивает бронирование
    private final PaymentTransactionService paymentTransactionService;

    public PayingController(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payForBooking(@PathVariable("id") long bookingId) {
        try {
            paymentTransactionService.payForBooking(bookingId);
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Оплата прошла успешно");

            return ResponseEntity.ok(res.toString());
        } catch (Exception e) {
            log.error("НЕ УДАЛОСЬ ОПЛАТИТЬ ОФФЕР: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
