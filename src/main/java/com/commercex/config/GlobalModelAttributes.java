package com.commercex.config;

import java.security.Principal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentUserName")
    public String currentUserName(Principal principal) {
        if (principal == null) {
            return null;
        }
        return principal.getName();
    }
}