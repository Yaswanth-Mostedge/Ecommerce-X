package com.commercex.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String brand;

    private String category;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Builder.Default
    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Builder.Default
    private Integer stock = 0;

    private String imageUrl;

    @Builder.Default
    private Double rating = 4.5;

    @Builder.Default
    private boolean active = true;

    @Transient
    public BigDecimal getSalePrice() {

        if (price == null) {
            return BigDecimal.ZERO;
        }

        if (discountPercent == null ||
                discountPercent.compareTo(BigDecimal.ZERO) <= 0) {

            return price.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal discountAmount = price
                .multiply(discountPercent)
                .divide(
                        BigDecimal.valueOf(100),
                        4,
                        RoundingMode.HALF_UP
                );

        return price
                .subtract(discountAmount)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }
}