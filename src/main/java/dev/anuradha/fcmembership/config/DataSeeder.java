package dev.anuradha.fcmembership.config;

import dev.anuradha.fcmembership.entity.*;
import dev.anuradha.fcmembership.enums.CriteriaType;
import dev.anuradha.fcmembership.enums.PlanDuration;
import dev.anuradha.fcmembership.enums.TierType;
import dev.anuradha.fcmembership.repository.MembershipPlanRepository;
import dev.anuradha.fcmembership.repository.MembershipTierRepository;
import dev.anuradha.fcmembership.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final MembershipPlanRepository planRepository;
    private final MembershipTierRepository tierRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (planRepository.count() > 0) {
            log.info("Data already seeded, skipping...");
            return;
        }

        log.info("Seeding membership plans, tiers and benefits...");

        seedPlans();
        seedUsers();

        log.info("Seeding complete.");
    }

    private void seedPlans() {

        // ── MONTHLY PLAN ──────────────────────────────────────────
        MembershipPlan monthly = planRepository.save(
                MembershipPlan.builder()
                        .name("Monthly Membership")
                        .duration(PlanDuration.MONTHLY)
                        .price(new BigDecimal("99.00"))
                        .durationInDays(30)
                        .description("Flexible monthly membership with core benefits")
                        .active(true)
                        .build()
        );
        attachTiers(monthly,
                TierConfig.of(TierType.SILVER, "Silver", 1,
                        "Basic perks for monthly members",
                        5.0, true, false,
                        CriteriaType.MIN_ORDER_COUNT, 2, null, null),
                TierConfig.of(TierType.GOLD, "Gold", 2,
                        "Enhanced perks for active monthly members",
                        10.0, true, false,
                        CriteriaType.MIN_ORDER_VALUE, null, new BigDecimal("1000"), null),
                TierConfig.of(TierType.PLATINUM, "Platinum", 3,
                        "Premium perks for top monthly members",
                        15.0, true, true,
                        CriteriaType.MIN_ORDER_VALUE, null, new BigDecimal("3000"), null)
        );

        // ── QUARTERLY PLAN ────────────────────────────────────────
        MembershipPlan quarterly = planRepository.save(
                MembershipPlan.builder()
                        .name("Quarterly Membership")
                        .duration(PlanDuration.QUARTERLY)
                        .price(new BigDecimal("249.00"))
                        .durationInDays(90)
                        .description("Quarterly membership with better savings")
                        .active(true)
                        .build()
        );
        attachTiers(quarterly,
                TierConfig.of(TierType.SILVER, "Silver", 1,
                        "Basic perks for quarterly members",
                        7.0, true, false,
                        CriteriaType.MIN_ORDER_COUNT, 5, null, null),
                TierConfig.of(TierType.GOLD, "Gold", 2,
                        "Enhanced perks for active quarterly members",
                        12.0, true, false,
                        CriteriaType.MIN_ORDER_VALUE, null, new BigDecimal("2000"), null),
                TierConfig.of(TierType.PLATINUM, "Platinum", 3,
                        "Premium perks for top quarterly members",
                        18.0, true, true,
                        CriteriaType.USER_COHORT, null, null, "PREMIUM_INVITE")
        );

        // ── YEARLY PLAN ───────────────────────────────────────────
        MembershipPlan yearly = planRepository.save(
                MembershipPlan.builder()
                        .name("Yearly Membership")
                        .duration(PlanDuration.YEARLY)
                        .price(new BigDecimal("799.00"))
                        .durationInDays(365)
                        .description("Best value yearly membership with all perks")
                        .active(true)
                        .build()
        );
        attachTiers(yearly,
                TierConfig.of(TierType.SILVER, "Silver", 1,
                        "Basic perks for yearly members",
                        10.0, true, false,
                        CriteriaType.MIN_ORDER_COUNT, 3, null, null),
                TierConfig.of(TierType.GOLD, "Gold", 2,
                        "Enhanced perks for active yearly members",
                        15.0, true, false,
                        CriteriaType.MIN_ORDER_VALUE, null, new BigDecimal("1500"), null),
                TierConfig.of(TierType.PLATINUM, "Platinum", 3,
                        "Premium perks for top yearly members",
                        20.0, true, true,
                        CriteriaType.MIN_ORDER_VALUE, null, new BigDecimal("5000"), null)
        );

        log.info("Seeded 3 plans with tiers and benefits.");
    }

    private void attachTiers(MembershipPlan plan, TierConfig... configs) {
        for (TierConfig config : configs) {

            MembershipTier tier = tierRepository.save(
                    MembershipTier.builder()
                            .tierType(config.tierType)
                            .name(config.name)
                            .description(config.description)
                            .tierLevel(config.tierLevel)
                            .active(true)
                            .plan(plan)
                            .build()
            );

            // Benefits
            TierBenefit discount = TierBenefit.builder()
                    .benefitType("DISCOUNT")
                    .description(config.discountPct + "% discount on selected categories")
                    .discountPercentage(BigDecimal.valueOf(config.discountPct))
                    .freeDelivery(false)
                    .featureEnabled(false)
                    .active(true)
                    .tier(tier)
                    .build();

            TierBenefit delivery = TierBenefit.builder()
                    .benefitType("FREE_DELIVERY")
                    .description("Free delivery on eligible orders")
                    .discountPercentage(BigDecimal.ZERO)
                    .freeDelivery(config.freeDelivery)
                    .featureEnabled(false)
                    .active(true)
                    .tier(tier)
                    .build();

            TierBenefit support = TierBenefit.builder()
                    .benefitType("PRIORITY_SUPPORT")
                    .description("Priority customer support")
                    .discountPercentage(BigDecimal.ZERO)
                    .freeDelivery(false)
                    .featureEnabled(config.prioritySupport)
                    .active(true)
                    .tier(tier)
                    .build();

            tier.setBenefits(new ArrayList<>(List.of(discount, delivery, support)));
            tier.getBenefits().forEach(b -> b.setTier(tier));

            TierCriteria criteria = TierCriteria.builder()
                    .criteriaType(config.criteriaType)
                    .minOrderCount(config.minOrderCount)
                    .minOrderValue(config.minOrderValue)
                    .cohortName(config.cohortName)
                    .tier(tier)
                    .build();

            tier.setCriteriaList(new ArrayList<>(List.of(criteria)));
            tier.getCriteriaList().forEach(c -> c.setTier(tier));

            tierRepository.save(tier);
        }
    }

    private void seedUsers() {
        userRepository.save(User.builder()
                .name("Rahul Sharma").email("rahul@example.com")
                .cohort("REGULAR").totalOrderCount(8)
                .totalOrderValueThisMonth(new BigDecimal("3500"))
                .build());

        userRepository.save(User.builder()
                .name("Priya Nair").email("priya@example.com")
                .cohort("PREMIUM_INVITE").totalOrderCount(3)
                .totalOrderValueThisMonth(new BigDecimal("800"))
                .build());

        userRepository.save(User.builder()
                .name("Amit Verma").email("amit@example.com")
                .cohort("REGULAR").totalOrderCount(1)
                .totalOrderValueThisMonth(new BigDecimal("200"))
                .build());

        log.info("Seeded 3 demo users.");
    }

    // ── Inner config record to keep seedPlans() clean ─────────────
    private record TierConfig(
            TierType tierType, String name, int tierLevel, String description,
            double discountPct, boolean freeDelivery, boolean prioritySupport,
            CriteriaType criteriaType, Integer minOrderCount,
            BigDecimal minOrderValue, String cohortName
    ) {
        static TierConfig of(TierType tierType, String name, int tierLevel,
                             String description, double discountPct,
                             boolean freeDelivery, boolean prioritySupport,
                             CriteriaType criteriaType, Integer minOrderCount,
                             BigDecimal minOrderValue, String cohortName) {
            return new TierConfig(tierType, name, tierLevel, description,
                    discountPct, freeDelivery, prioritySupport,
                    criteriaType, minOrderCount, minOrderValue, cohortName);
        }
    }
}
