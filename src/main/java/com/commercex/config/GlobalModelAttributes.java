package com.commercex.config;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.commercex.model.AppUser;
import com.commercex.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

/** Exposes the signed-in user's display name to every template, e.g. for the navbar/sidebar profile menu. */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final AppUserRepository users;

    @ModelAttribute("currentUserName")
    public String currentUserName(Principal principal) {
        if (principal == null) {
            return null;
        }
        return users.findByEmailIgnoreCase(principal.getName())
                .map(AppUser::getName)
                .orElse(principal.getName());
    }
}
