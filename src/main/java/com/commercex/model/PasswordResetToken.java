package com.commercex.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PasswordResetToken {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(unique=true,nullable=false) private String token;
    @OneToOne(optional=false) private AppUser user;
    private LocalDateTime expiresAt;
}
