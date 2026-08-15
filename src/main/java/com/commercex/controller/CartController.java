package com.commercex.controller;

import com.commercex.model.Coupon;
import com.commercex.model.Product;
import com.commercex.repository.CouponRepository;
import com.commercex.repository.ProductRepository;
import com.commercex.service.CommerceService;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final ProductRepository products;
    private final CouponRepository coupons;
    private final CommerceService service;

    @GetMapping("/cart")
    public String cart(Model model, HttpSession session) {

        Map<Long, Integer> cart = getCart(session);

        List<Product> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        Iterator<Map.Entry<Long, Integer>> iterator =
                cart.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<Long, Integer> entry = iterator.next();

            Product product =
                    products.findById(entry.getKey()).orElse(null);

            if (product == null || !product.isActive() ||
                    product.getStock() == null ||
                    product.getStock() <= 0) {

                iterator.remove();
                continue;
            }

            int quantity =
                    Math.min(
                            Math.max(entry.getValue(), 1),
                            product.getStock()
                    );

            entry.setValue(quantity);

            items.add(product);

            subtotal = subtotal.add(
                    product.getSalePrice()
                            .multiply(
                                    BigDecimal.valueOf(quantity)
                            )
            );
        }

        Object couponCode =
                session.getAttribute("coupon");

        Coupon coupon =
                couponCode == null
                        ? null
                        : service.findActiveCoupon(
                                couponCode.toString()
                        );

        CommerceService.Totals totals =
                service.computeTotals(subtotal, coupon);

        model.addAttribute("items", items);
        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", totals.subtotal());
        model.addAttribute("discount", totals.discount());
        model.addAttribute("tax", totals.tax());
        model.addAttribute("shipping", totals.shipping());
        model.addAttribute("total", totals.total());
        model.addAttribute("coupon", coupon);

        return "cart";
    }

    @PostMapping("/cart/add/{id}")
    public String add(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int qty,
            HttpSession session) {

        Product product =
                products.findById(id).orElseThrow();

        if (!product.isActive() ||
                product.getStock() == null ||
                product.getStock() <= 0) {

            return "redirect:/shop";
        }

        int safeQty =
                Math.max(qty, 1);

        Map<Long, Integer> cart =
                getCart(session);

        int existing =
                cart.getOrDefault(id, 0);

        cart.put(
                id,
                Math.min(
                        existing + safeQty,
                        product.getStock()
                )
        );

        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String update(
            @RequestParam Long id,
            @RequestParam int qty,
            HttpSession session) {

        Map<Long, Integer> cart =
                getCart(session);

        if (qty <= 0) {
            cart.remove(id);
            return "redirect:/cart";
        }

        Product product =
                products.findById(id).orElse(null);

        if (product == null ||
                !product.isActive() ||
                product.getStock() == null ||
                product.getStock() <= 0) {

            cart.remove(id);
            return "redirect:/cart";
        }

        cart.put(
                id,
                Math.min(qty, product.getStock())
        );

        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{id}")
    public String remove(
            @PathVariable Long id,
            HttpSession session) {

        getCart(session).remove(id);

        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clear(HttpSession session) {

        getCart(session).clear();
        session.removeAttribute("coupon");

        return "redirect:/cart";
    }

    @PostMapping("/coupon")
    public String applyCoupon(
            @RequestParam String code,
            HttpSession session) {

        String normalized =
                code == null
                        ? ""
                        : code.trim().toUpperCase();

        if (normalized.isBlank()) {
            session.removeAttribute("coupon");
            return "redirect:/cart";
        }

        Optional<Coupon> coupon =
                coupons.findByCodeIgnoreCaseAndActiveTrue(
                        normalized
                );

        if (coupon.isPresent()) {
            session.setAttribute(
                    "coupon",
                    coupon.get().getCode()
            );
        } else {
            session.removeAttribute("coupon");
        }

        return "redirect:/cart";
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCart(
            HttpSession session) {

        Object value =
                session.getAttribute("cart");

        if (value instanceof Map<?, ?> map) {

            return (Map<Long, Integer>) map;
        }

        Map<Long, Integer> cart =
                new LinkedHashMap<>();

        session.setAttribute(
                "cart",
                cart
        );

        return cart;
    }
}
