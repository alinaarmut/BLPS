package org.example.controllers;


import org.example.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/paying")
public class PayingController {

    // post - pay метод - гость оплачивает бронирование
    private final PaymentService payingService;

    public PayingController(PaymentService payingService) {
        this.payingService = payingService;
    }
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payForBooking(@PathVariable("id") long bookingId) {
        return payingService.payForBooking(bookingId);
    }
}
