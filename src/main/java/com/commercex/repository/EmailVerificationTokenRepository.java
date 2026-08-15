package com.commercex.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commercex.model.EmailVerificationToken;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    void deleteByUserId(Long userId);
}