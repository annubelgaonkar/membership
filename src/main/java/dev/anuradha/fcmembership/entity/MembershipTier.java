package dev.anuradha.fcmembership.entity;

import dev.anuradha.fcmembership.enums.TierType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;

@Entity
@Table(name = "membership_tiers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MembershipTier extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TierType tierType;

    @Column(nullable = false)
    private String name;                //display name

    private String description;

    @Column(nullable = false)
    private Integer tierLevel;                      // 1 = Silver, 2 = Gold, 3 = Platinum

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;

    // Benefits configured per tier
    @OneToMany(mappedBy = "tier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TierBenefit> benefits = new ArrayList<>();

    // Criteria that qualifies a user for this tier
    @OneToMany(mappedBy = "tier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TierCriteria> criteriaList = new ArrayList<>();

}
