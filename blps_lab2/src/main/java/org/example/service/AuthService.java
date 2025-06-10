package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.JwtResponse;
import org.example.entity.LoginRequest;
import org.example.entity.primary.Token;
import org.example.entity.primary.User;
import org.example.entity.enums_status.TokenType;
import org.example.repository.primary.TokenRepository;
import org.example.repository.primary.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public JwtResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByName(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        String jwtToken = jwtService.generateToken(user);

        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();

        tokenRepository.save(token);

        return new JwtResponse(jwtToken);
    }
}