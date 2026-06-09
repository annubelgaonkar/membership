package dev.anuradha.fcmembership.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    //for discount benefits
    @Column(nullable = false)
    private BigDecimal discountPercentage;

    //for delivery benefits
    private Boolean freeDelivery;

    //for early access / priority Support
    private Boolean featureEnabled;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;


}
