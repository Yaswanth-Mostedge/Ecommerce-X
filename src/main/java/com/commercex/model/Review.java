package com.commercex.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Getter @Setter @NoArgsConstructor
public class Review {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private Product product;
    @ManyToOne(optional=false) private AppUser user;
    @Min(1) @Max(5) private int rating;
    @Size(max=1000) private String comment;
    private LocalDateTime createdAt = LocalDateTime.now();
}
