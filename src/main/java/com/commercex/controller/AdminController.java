package com.commercex.controller;

import com.commercex.model.*;
import com.commercex.repository.*;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    /** Statuses an admin is allowed to set. */
    private static final Set<String> ALLOWED_STATUSES =
            Set.of("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED");

    private final ProductRepository products;
    private final OrderRepository orders;
    private final AppUserRepository users;
    private final CouponRepository coupons;
    private final ReviewRepository reviews;

    public AdminController(ProductRepository p, OrderRepository o,
                           AppUserRepository u, CouponRepository c,
                           ReviewRepository r) {
        products = p;
        orders = o;
        users = u;
        coupons = c;
        reviews = r;
    }

    /** Newest first, tolerating orders with no timestamp. */
    private List<Order> newestFirst() {
        return orders.findAll().stream()
                .sorted(Comparator.comparing(
                        Order::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @GetMapping({"", "/", "/dashboard"})
    String dashboard(Model m) {
        m.addAttribute("productCount", products.count());
        m.addAttribute("orderCount", orders.count());
        m.addAttribute("userCount", users.count());
        m.addAttribute("products", products.findAll());
        m.addAttribute("orders", newestFirst().stream().limit(10).toList());
        return "admin-dashboard";
    }

    @GetMapping("/products")
    String productPage(Model m) {
        m.addAttribute("products", products.findAll());
        m.addAttribute("product", new Product());
        return "admin-products";
    }

    @PostMapping("/products/save")
    String save(@Valid @ModelAttribute("product") Product p,
                BindingResult binding, Model m) {

        // Without a BindingResult a rejected field threw straight to a 500.
        if (binding.hasErrors()) {
            m.addAttribute("products", products.findAll());
            m.addAttribute("error", "Please check the product details.");
            return "admin-products";
        }

        products.save(p);
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    @Transactional
    String delete(@PathVariable Long id) {
        // Reviews reference products, so deleting one outright violated
        // the foreign key and returned a 500.
        products.findById(id).ifPresent(p -> {
            reviews.deleteAll(reviews.findByProductOrderByCreatedAtDesc(p));
            products.delete(p);
        });
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/stock")
    String stock(@PathVariable Long id, @RequestParam int stock) {
        products.findById(id).ifPresent(p -> {
            p.setStock(Math.max(0, stock));
            products.save(p);
        });
        return "redirect:/admin/products";
    }

    @GetMapping("/orders")
    String orderPage(Model m) {
        m.addAttribute("orders", newestFirst());
        return "admin-orders";
    }

    @PostMapping("/orders/{id}/status")
    @Transactional
    String status(@PathVariable Long id, @RequestParam String status) {

        String next = status == null ? "" : status.trim().toUpperCase();

        if (!ALLOWED_STATUSES.contains(next)) {
            return "redirect:/admin/orders";
        }

        orders.findByIdWithItems(id).ifPresent(o -> {

            String previous = o.getStatus();

            // Cancelling from the admin console used to skip the stock
            // restoration the customer-facing cancel performs, so inventory
            // was silently lost.
            if ("CANCELLED".equals(next) && !"CANCELLED".equals(previous)) {
                OrderController.restoreStock(o, products);
            }

            o.setStatus(next);

            if ("DELIVERED".equals(next)) {
                o.setPaymentStatus(
                        "COD".equals(o.getPaymentMethod())
                                ? "PAID"
                                : o.getPaymentStatus());
            }

            orders.save(o);
        });

        return "redirect:/admin/orders";
    }

    @GetMapping("/coupons")
    String couponPage(Model m) {
        m.addAttribute("coupons", coupons.findAll());
        m.addAttribute("coupon", new Coupon());
        return "admin-coupons";
    }

    @PostMapping("/coupons")
    String coupon(@ModelAttribute Coupon c, Model m) {

        String code = c.getCode() == null ? "" : c.getCode().trim().toUpperCase();

        // A null code threw an NPE; a blank one saved an unusable coupon.
        if (code.isBlank()
                || c.getPercent() == null
                || c.getPercent().signum() <= 0) {

            m.addAttribute("coupons", coupons.findAll());
            m.addAttribute("error", "Enter a coupon code and a percentage above zero.");
            return "admin-coupons";
        }

        if (c.getId() == null && coupons.existsByCodeIgnoreCase(code)) {
            m.addAttribute("coupons", coupons.findAll());
            m.addAttribute("error", "That coupon code already exists.");
            return "admin-coupons";
        }

        c.setCode(code);
        coupons.save(c);
        return "redirect:/admin/coupons";
    }
}
