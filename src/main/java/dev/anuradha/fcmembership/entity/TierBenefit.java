package dev.anuradha.fcmembership.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tier_benefits")
@Setter @Getter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TierBenefit extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String benefitType;

    @Column(nullable = false)
    private String description;             //human readable description



}
