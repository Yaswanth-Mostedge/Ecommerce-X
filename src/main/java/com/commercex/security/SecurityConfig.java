package com.commercex.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            boolean admin = authentication.getAuthorities()
                    .stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

            response.sendRedirect(
                    request.getContextPath() + (admin ? "/admin" : "/shop")
            );
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationSuccessHandler successHandler) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/shop", "/product/**",
                    "/login", "/register",
                    "/admin/login",
                    "/forgot-password", "/reset-password",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico"
                ).permitAll()

                .requestMatchers("/admin/**").hasRole("ADMIN")

                .requestMatchers(
                    "/cart/**",
                    "/checkout/**",
                    "/orders/**",
                    "/order/success"
                ).authenticated()

                .anyRequest().permitAll()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/shop?logout=true")
                .permitAll()
            );

        return http.build();
    }
}
