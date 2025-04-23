package org.example.controllers;


import org.example.service.OfferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/offer")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }


    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/accept/{offerId}")
    public ResponseEntity<String> acceptOffer(@PathVariable("offerId") Long offerId) {
        String result = offerService.acceptOffer(offerId);

        if (result.contains("не найден")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        } else if (result.contains("cannot be accepted")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } else {
            return ResponseEntity.ok(result);
        }
    }

    // отклонение оффера после подтверждения хоста
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/reject/{offerId}")
    public ResponseEntity<String> rejectOffer(@PathVariable Long offerId) {
        String result = offerService.rejectOffer(offerId);
        if (result.contains("не найден")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        } else {
            return ResponseEntity.ok(result);
        }
    }
}
