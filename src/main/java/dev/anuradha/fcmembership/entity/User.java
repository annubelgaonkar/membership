package dev.anuradha.fcmembership.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Setter @Getter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String cohort = "REGULAR";

    @Column(nullable = false)
    private Integer totalOrderCount = 0;

    private java.math.BigDecimal totalOrderValueThisMonth = BigDecimal.ZERO;

}
