package dev.anuradha.fcmembership.entity;

import dev.anuradha.fcmembership.enums.CriteriaType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tier_criteria")
@Setter @Getter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TierCriteria extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CriteriaType criteriaType;

    private Integer minOrderCount;

    @Column(precision = 10, scale = 2)
    private BigDecimal minOrderValue;

    //  e.g. "PREMIUM_INVITE", "EARLY_ADOPTER
    private String cohortName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

}
