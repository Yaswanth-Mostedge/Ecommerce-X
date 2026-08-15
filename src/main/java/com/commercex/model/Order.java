package com.commercex.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
@Entity @Getter @Setter @NoArgsConstructor
@Table(name="orders")
public class Order {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private AppUser user;
    private String customerName, email, phone, address, city, state, zip, country;
    private String status = "PENDING";
    private String paymentMethod = "COD";
    private String paymentStatus = "PENDING";
    private String transactionId;
    private BigDecimal subtotal = BigDecimal.ZERO, discount = BigDecimal.ZERO, tax = BigDecimal.ZERO,
            shipping = BigDecimal.ZERO, total = BigDecimal.ZERO;
    private LocalDateTime createdAt = LocalDateTime.now();
    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true)
    @JoinColumn(name="order_id")
    private List<OrderItem> items = new ArrayList<>();
}
