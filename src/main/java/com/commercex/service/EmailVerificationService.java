package com.commercex.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commercex.model.AppUser;
import com.commercex.model.EmailVerificationToken;
import com.commercex.repository.AppUserRepository;
import com.commercex.repository.EmailVerificationTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final AppUserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${commercex.base-url}")
    private String baseUrl;

    @Value("${commercex.mail.from}")
    private String from;

    @Transactional
    public void createAndSendToken(AppUser user) {

        tokenRepository.deleteByUserId(user.getId());

        String tokenValue =
                UUID.randomUUID().toString();

        EmailVerificationToken token =
                EmailVerificationToken.builder()
                        .token(tokenValue)
                        .user(user)
                        .expiresAt(
                                LocalDateTime.now()
                                        .plusHours(24)
                        )
                        .build();

        tokenRepository.save(token);

        String verificationLink =
                baseUrl
                        + "/verify-email?token="
                        + tokenValue;

        try {
            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setFrom(from);
            message.setTo(user.getEmail());
            message.setSubject(
                    "Verify your CommerceX account"
            );

            message.setText(
                    "Hello " + user.getName() + ",\n\n"
                    + "Please verify your CommerceX account "
                    + "using the link below:\n\n"
                    + verificationLink
                    + "\n\n"
                    + "This link expires in 24 hours."
            );

            mailSender.send(message);

        } catch (Exception ignored) {
            // Without SMTP configured the token still stands, so the
            // account is not left un-verifiable during local runs.
        }
    }

    @Transactional
    public boolean verify(String tokenValue) {

        EmailVerificationToken token =
                tokenRepository
                        .findByToken(tokenValue)
                        .orElse(null);

        if (token == null) {
            return false;
        }

        if (token.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            tokenRepository.delete(token);
            return false;
        }

        AppUser user = token.getUser();

        user.setEmailVerified(true);
        user.setEnabled(true);

        userRepository.save(user);

        tokenRepository.delete(token);

        return true;
    }
}