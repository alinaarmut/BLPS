package org.example.controllers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.JwtResponse;
import org.example.entity.LoginRequest;
import org.example.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final AuthService authenticationService;

    @PostMapping
    public JwtResponse login(@RequestBody LoginRequest request) {
        log.info("Login attempt: {}", request.getUsername());
        return authenticationService.authenticate(request);
    }
}