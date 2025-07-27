package com.app.authservice.service;


import com.app.authservice.model.User;
import com.app.authservice.model.VerificationToken;
import com.app.authservice.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationTokenService {

    private static final int EXPIRATION_MINUTES = 60 * 24; // 24 hours

    @Autowired private VerificationTokenRepository tokenRepository;

    public VerificationToken createVerificationToken(User user) {
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        return tokenRepository.save(token);
    }

    public Optional<VerificationToken> findByToken(String token) {
        return tokenRepository.findByToken(token);
    }

    public boolean isTokenExpired(VerificationToken token) {
        return token.getExpiryDate().isBefore(LocalDateTime.now());
    }
}
