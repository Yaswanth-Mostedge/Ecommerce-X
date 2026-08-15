package com.commercex.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.commercex.model.AppUser;
import com.commercex.model.Order;
import com.commercex.repository.AppUserRepository;
import com.commercex.service.CommerceService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CheckoutController {

    private final CommerceService service;

    private final AppUserRepository appUserRepository;


    // =========================================================
    // CHECKOUT PAGE
    // =========================================================

    @GetMapping("/checkout")
    public String checkout(
            Model model,
            HttpSession session) {

        Map<Long, Integer> cart =
                getCart(session);

        if (cart.isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute(
                "cart",
                cart
        );

        return "checkout";
    }


    // =========================================================
    // PLACE ORDER
    // =========================================================

    @PostMapping("/checkout")
    public String placeOrder(

            @RequestParam String customerName,

            @RequestParam String email,

            @RequestParam String phone,

            @RequestParam String address,

            @RequestParam String city,

            @RequestParam String state,

            @RequestParam String zip,

            @RequestParam String country,

            @RequestParam(
                    defaultValue = "COD"
            )
            String paymentMethod,

            HttpSession session,

            Model model,

            Authentication authentication) {

        try {

            // -------------------------------------------------
            // CHECK LOGIN
            // -------------------------------------------------

            if (authentication == null ||
                    !authentication.isAuthenticated() ||
                    "anonymousUser".equals(
                            authentication.getName())) {

                return "redirect:/login";
            }


            // -------------------------------------------------
            // GET CART
            // -------------------------------------------------

            Map<Long, Integer> cart =
                    getCart(session);

            if (cart.isEmpty()) {

                model.addAttribute(
                        "error",
                        "Your cart is empty."
                );

                return "checkout";
            }


            // -------------------------------------------------
            // FIND LOGGED-IN USER
            // -------------------------------------------------

            String loggedInEmail =
                    authentication.getName();

            AppUser user =
                    appUserRepository
                            .findByEmailIgnoreCase(
                                    loggedInEmail
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "User account not found."
                                    )
                            );


            // -------------------------------------------------
            // COUPON
            // -------------------------------------------------

            String coupon =
                    (String) session.getAttribute(
                            "coupon"
                    );


            // -------------------------------------------------
            // CREATE ORDER
            // -------------------------------------------------

            Order order =
                    service.placeOrder(

                            user,

                            cart,

                            customerName,

                            email,

                            phone,

                            address,

                            city,

                            state,

                            zip,

                            country,

                            paymentMethod,

                            coupon
                    );


            // -------------------------------------------------
            // CLEAR CART
            // -------------------------------------------------

            session.removeAttribute("cart");

            session.removeAttribute("coupon");


            // -------------------------------------------------
            // SAVE ORDER ID
            // -------------------------------------------------

            session.setAttribute(
                    "lastOrderId",
                    order.getId()
            );


            return "redirect:/order/success";


        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "checkout";


        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    "Unable to place your order. Please try again."
            );

            return "checkout";
        }
    }


    // =========================================================
    // ORDER SUCCESS
    // =========================================================

    @GetMapping("/order/success")
    public String success(
            Model model,
            HttpSession session) {

        Object orderId =
                session.getAttribute(
                        "lastOrderId"
                );

        if (orderId == null) {
            return "redirect:/shop";
        }

        model.addAttribute(
                "orderId",
                orderId
        );

        return "success";
    }


    // =========================================================
    // CART HELPER
    // =========================================================

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCart(
            HttpSession session) {

        Object cartObject =
                session.getAttribute("cart");

        if (cartObject instanceof Map<?, ?>) {

            return (Map<Long, Integer>)
                    cartObject;
        }

        Map<Long, Integer> cart =
                new HashMap<>();

        session.setAttribute(
                "cart",
                cart
        );

        return cart;
    }
}