package com.commercex.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.commercex.model.AppUser;
import com.commercex.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(
            String email)
            throws UsernameNotFoundException {

        AppUser appUser =
                appUserRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "User not found"
                                )
                        );

        return User.builder()
                .username(appUser.getEmail())
                .password(appUser.getPassword())
                .authorities(
                        new SimpleGrantedAuthority(
                                "ROLE_" +
                                appUser.getRole().name()
                        )
                )
                .disabled(!appUser.isEnabled())
                .build();
    }
}