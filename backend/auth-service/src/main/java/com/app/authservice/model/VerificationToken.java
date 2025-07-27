package com.app.authservice.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_tokens")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String token;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable=false, name="user_id")
    private User user;

    private LocalDateTime expiryDate;
}
