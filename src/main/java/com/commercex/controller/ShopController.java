package com.commercex.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.commercex.model.Product;
import com.commercex.repository.BannerRepository;
import com.commercex.repository.ProductRepository;
import com.commercex.repository.ReviewRepository;
import com.commercex.web.ProductNotFoundException;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ShopController {

    private final ProductRepository products;
    private final ReviewRepository reviews;
    private final BannerRepository banners;

    @GetMapping({"/", "/shop"})
    public String shop(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            Model model,
            HttpSession session) {

        List<Product> list =
                products.findByActiveTrueOrderByIdDesc();

        // SEARCH
        if (q != null && !q.isBlank()) {

            String search = q.trim().toLowerCase();

            list = list.stream()
                    .filter(p ->
                            (p.getName() != null &&
                                    p.getName()
                                            .toLowerCase()
                                            .contains(search))
                            ||
                            (p.getCategory() != null &&
                                    p.getCategory()
                                            .toLowerCase()
                                            .contains(search))
                            ||
                            (p.getBrand() != null &&
                                    p.getBrand()
                                            .toLowerCase()
                                            .contains(search))
                    )
                    .toList();
        }

        // CATEGORY
        if (category != null && !category.isBlank()) {

            list = list.stream()
                    .filter(p ->
                            p.getCategory() != null &&
                            category.equalsIgnoreCase(
                                    p.getCategory()
                            )
                    )
                    .toList();
        }

        // PRICE LOW -> HIGH
        if ("low".equalsIgnoreCase(sort)) {

            list = list.stream()
                    .sorted(
                            Comparator.comparing(
                                    Product::getSalePrice
                            )
                    )
                    .toList();
        }

        // PRICE HIGH -> LOW
        if ("high".equalsIgnoreCase(sort)) {

            list = list.stream()
                    .sorted(
                            Comparator.comparing(
                                    Product::getSalePrice
                            ).reversed()
                    )
                    .toList();
        }

        // RATING HIGH -> LOW
        if ("rating".equalsIgnoreCase(sort)) {

            list = list.stream()
                    .sorted(
                            Comparator.comparing(
                                    Product::getRating,
                                    Comparator.nullsLast(
                                            Comparator.reverseOrder()
                                    )
                            )
                    )
                    .toList();
        }

        // MODEL
        model.addAttribute("products", list);

        model.addAttribute(
                "q",
                q == null ? "" : q
        );

        model.addAttribute(
                "category",
                category == null ? "" : category
        );

        model.addAttribute(
                "sort",
                sort == null ? "" : sort
        );

        // CATEGORIES
        List<String> categories =
                products.findAll()
                        .stream()
                        .map(Product::getCategory)
                        .filter(Objects::nonNull)
                        .filter(c -> !c.isBlank())
                        .distinct()
                        .sorted()
                        .toList();

        model.addAttribute(
                "categories",
                categories
        );

        model.addAttribute(
                "banner",
                banners.findTopByOrderByIdAsc().orElse(null)
        );

        return "shop";
    }

    // PRODUCT DETAILS
    @GetMapping("/product/{id}")
    public String product(
            @PathVariable Long id,
            Model model) {

        Product product =
                products.findById(id)
                        .filter(Product::isActive)
                        .orElseThrow(() ->
                                new ProductNotFoundException(
                                        "Product not found: " + id
                                )
                        );

        model.addAttribute(
                "product",
                product
        );

        model.addAttribute(
                "salePrice",
                product.getSalePrice()
        );

        model.addAttribute(
                "reviews",
                reviews.findByProductOrderByCreatedAtDesc(product)
        );

        return "product";
    }
}