package com.commercex.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.persistence.EntityNotFoundException;

/**
 * Renders a branded error page instead of the Whitelabel page, which
 * leaked full stack traces to the browser on any unhandled failure.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({
            ProductNotFoundException.class,
            EntityNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(RuntimeException ex, Model model) {

        model.addAttribute("status", 404);
        model.addAttribute("heading", "Page not found");
        model.addAttribute(
                "message",
                "The page or product you are looking for is not available."
        );

        return "error";
    }


    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String forbidden(ForbiddenException ex, Model model) {

        model.addAttribute("status", 403);
        model.addAttribute("heading", "Not allowed");
        model.addAttribute(
                "message",
                "You do not have access to that item."
        );

        return "error";
    }


    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String badRequest(IllegalArgumentException ex, Model model) {

        model.addAttribute("status", 400);
        model.addAttribute("heading", "That did not work");
        model.addAttribute("message", ex.getMessage());

        return "error";
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String serverError(Exception ex, Model model) {

        // Logged in full for the operator; never shown to the visitor.
        log.error("Unhandled error", ex);

        model.addAttribute("status", 500);
        model.addAttribute("heading", "Something went wrong");
        model.addAttribute(
                "message",
                "We hit an unexpected problem. Please try again."
        );

        return "error";
    }
}
