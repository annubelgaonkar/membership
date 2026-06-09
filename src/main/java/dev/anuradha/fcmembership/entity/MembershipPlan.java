package dev.anuradha.fcmembership.entity;

import dev.anuradha.fcmembership.enums.PlanDuration;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "membership_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipPlan extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;                    // for eg: MONTHLY Basic

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanDuration  duration;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;           //for eg : 99.00, 199.00, 249.00

    @Column(nullable = false)
    private Integer durationInDays;         //for eg 30, 90, 180 in days

    private String description;

    @Column(nullable = false)
    private Boolean active = true;

    // one plan can have multiple tiers, 3 for now -> SILVER, GOLD, PLATINUM
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MembershipTier> tiers = new ArrayList<>();

}
