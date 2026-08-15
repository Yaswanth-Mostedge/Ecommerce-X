package com.commercex.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.commercex.model.AppUser;
import com.commercex.service.CommerceService;
import com.commercex.service.EmailVerificationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {
	
	private final EmailVerificationService emailVerificationService;

    private final CommerceService commerceService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin-login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new AppUser());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("user") AppUser user,
            Model model) {

        if (user.getPassword() == null ||
                user.getPassword().length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters.");
            return "register";
        }

        try {
            // Delegates to the service so name/email rules live in one place.
            // Registering without a name previously reached the database and
            // failed with a 500 instead of a readable message.
            commerceService.register(
                    user.getName(),
                    user.getEmail(),
                    user.getPassword()
            );
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }

        return "redirect:/login?registered=true";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(
            @RequestParam String email,
            Model model) {

        String normalized = email == null
                ? ""
                : email.trim().toLowerCase();

        if (normalized.isBlank()) {
            model.addAttribute("error", "Enter your email address.");
            return "forgot-password";
        }

        String resetLink = commerceService.createAndSendPasswordResetLink(normalized);

        // In production SMTP, the same link is emailed.
        // The link is also shown here so the flow remains testable when SMTP is not configured.
        if (resetLink != null) {
            model.addAttribute("resetLink", resetLink);
        }

        model.addAttribute(
                "sent",
                "If an account exists for that email, a reset link is available."
        );

        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(
            @RequestParam String token,
            Model model) {

        model.addAttribute("token", token);
        return "reset-password";
    }
    
    
    @GetMapping("/verify-email")
    public String verifyEmail(
            @RequestParam String token,
            Model model) {

        boolean verified =
                emailVerificationService.verify(token);

        if (verified) {
            return "redirect:/login?verified=true";
        }

        model.addAttribute(
                "error",
                "Verification link is invalid or expired."
        );

        return "verify-result";
    }
    
    
    

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String token,
            @RequestParam String password,
            Model model) {

        if (password == null || password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        boolean success =
                commerceService.resetPassword(token, password);

        if (!success) {
            model.addAttribute(
                    "error",
                    "This reset link is invalid or expired."
            );
            model.addAttribute("token", token);
            return "reset-password";
        }

        return "redirect:/login?reset=true";
    }
}
