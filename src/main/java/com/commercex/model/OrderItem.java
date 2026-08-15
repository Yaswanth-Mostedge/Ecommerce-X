package com.commercex.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Entity @Getter @Setter @NoArgsConstructor
public class OrderItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;
    private BigDecimal price;
    private int quantity;
    public OrderItem(Long productId,String productName,String imageUrl,BigDecimal price,int quantity){
        this.productId=productId;this.productName=productName;this.imageUrl=imageUrl;this.price=price;this.quantity=quantity;
    }
}
