package dev.anuradha.fcmembership.entity;

import dev.anuradha.fcmembership.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import javax.swing.plaf.metal.MetalMenuBarUI;
import java.time.LocalDateTime;

@Entity
@Setter @Getter
@Table(name = "user_subscriptions")
@AllArgsConstructor @NoArgsConstructor
@Builder
public class UserSubscription extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    private LocalDateTime cancelledAt;

    /* * Optimistic locking — if two threads try to modify the same
    * subscription simultaneously, one will get an OptimisticLockException
    */
     @Version
    private Long version;

}
