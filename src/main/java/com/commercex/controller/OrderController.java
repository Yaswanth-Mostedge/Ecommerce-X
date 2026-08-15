package com.commercex.controller;

import com.commercex.model.*;
import com.commercex.repository.*;
import com.commercex.web.ForbiddenException;
import com.commercex.web.ProductNotFoundException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orders;
    private final AppUserRepository users;
    private final ProductRepository products;
    private final ReviewRepository reviews;

    public OrderController(OrderRepository o, AppUserRepository u,
                           ProductRepository p, ReviewRepository r) {
        orders = o;
        users = u;
        products = p;
        reviews = r;
    }

    private AppUser user() {
        return users.findByEmailIgnoreCase(
                SecurityContextHolder.getContext()
                        .getAuthentication().getName()
        ).orElseThrow();
    }

    /** Loads an order with its items, refusing anything owned by another user. */
    private Order ownedOrder(Long id) {
        Order order = orders.findByIdWithItems(id)
                .orElseThrow(() ->
                        new ProductNotFoundException("Order not found: " + id));

        if (!order.getUser().getId().equals(user().getId())) {
            throw new ForbiddenException("Order belongs to another user");
        }

        return order;
    }

    @GetMapping
    String list(Model m) {
        m.addAttribute("orders", orders.findByUserOrderByCreatedAtDesc(user()));
        return "orders";
    }

    @GetMapping("/{id}")
    String detail(@PathVariable Long id, Model m) {
        m.addAttribute("order", ownedOrder(id));
        return "order-detail";
    }

    @PostMapping("/{id}/cancel")
    @Transactional
    String cancel(@PathVariable Long id) {
        Order o = ownedOrder(id);

        if ("CONFIRMED".equals(o.getStatus()) || "PENDING".equals(o.getStatus())) {
            o.setStatus("CANCELLED");
            restoreStock(o);
            orders.save(o);
        }

        return "redirect:/orders/" + id;
    }

    /** Returns each line's quantity to inventory. Shared with admin cancellation. */
    static void restoreStock(Order order, ProductRepository products) {
        for (OrderItem i : order.getItems()) {
            products.findById(i.getProductId()).ifPresent(p -> {
                int current = p.getStock() == null ? 0 : p.getStock();
                p.setStock(current + i.getQuantity());
                products.save(p);
            });
        }
    }

    private void restoreStock(Order order) {
        restoreStock(order, products);
    }

    @PostMapping("/review/{productId}")
    @Transactional
    String review(@PathVariable Long productId,
                  @RequestParam int rating,
                  @RequestParam String comment) {

        Product p = products.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found: " + productId));

        Review r = new Review();
        r.setProduct(p);
        r.setUser(user());
        // Clamped so a hand-crafted form post cannot violate the entity
        // constraint and blow up with a 500.
        r.setRating(Math.min(5, Math.max(1, rating)));
        r.setComment(comment == null || comment.length() <= 1000
                ? comment
                : comment.substring(0, 1000));
        reviews.save(r);

        refreshRating(p);

        return "redirect:/product/" + productId;
    }

    /** Recomputes the product's headline rating from its reviews. */
    private void refreshRating(Product product) {
        List<Review> all = reviews.findByProductOrderByCreatedAtDesc(product);

        if (all.isEmpty()) {
            return;
        }

        double average = all.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0);

        product.setRating(Math.round(average * 10) / 10.0);
        products.save(product);
    }
}
