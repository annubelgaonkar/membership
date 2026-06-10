package dev.anuradha.fcmembership.mapper;

import dev.anuradha.fcmembership.dto.response.CriteriaResponse;
import dev.anuradha.fcmembership.dto.response.PlanResponse;
import dev.anuradha.fcmembership.dto.response.SubscriptionResponse;
import dev.anuradha.fcmembership.dto.response.TierResponse;
import dev.anuradha.fcmembership.entity.MembershipPlan;
import dev.anuradha.fcmembership.entity.MembershipTier;
import dev.anuradha.fcmembership.entity.TierCriteria;
import dev.anuradha.fcmembership.entity.UserSubscription;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Component
public class MembershipMapper {

    public CriteriaResponse toCriteriaResponse(TierCriteria criteria){
        return CriteriaResponse.builder()
                .id(criteria.getId())
                .criteriaType(criteria.getCriteriaType())
                .minOrderCount(criteria.getMinOrderCount())
                .minOrderValue(criteria.getMinOrderValue())
                .cohortName(criteria.getCohortName())
                .build();
    }

    public TierResponse toTierResponse(MembershipTier tier){
        List<CriteriaResponse> criteriaResponseList = tier.getCriteriaList() == null ?
                Collections.emptyList()
                :tier.getCriteriaList().stream().map(this::toCriteriaResponse)
                .toList();

        return TierResponse.builder()
                .id(tier.getId())
                .tierType(tier.getTierType())
                .name(tier.getName())
                .description(tier.getDescription())
                .tierLevel(tier.getTierLevel())
                .criteria(criteriaResponseList)
                .build();
    }
    public PlanResponse toPlanResponse(MembershipPlan plan){
        List<TierResponse> tiers = plan.getTiers() == null
                ? Collections.emptyList()
                :plan.getTiers().stream()
                        .filter(MembershipTier::getActive)
                        .map(this::toTierResponse)
                        .toList();

        return PlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .duration(plan.getDuration())
                .price(plan.getPrice())
                .durationInDays(plan.getDurationInDays())
                .description(plan.getDescription())
                .tiers(tiers)
                .build();
    }

    public SubscriptionResponse toSubscriptionResponse(UserSubscription sub){
        long daysRemaining = LocalDateTime.now().isBefore(sub.getExpiryDate())?
                ChronoUnit.DAYS.between(LocalDateTime.now(), sub.getExpiryDate())
                : 0;

        return SubscriptionResponse.builder()
                .subscriptionId(sub.getId())
                .userId(sub.getUser().getId())
                .userName(sub.getUser().getName())
                .plan(toPlanResponse(sub.getPlan()))
                .tier(toTierResponse(sub.getTier()))
                .status(sub.getStatus())
                .startDate(sub.getStartDate())
                .expiryDate(sub.getExpiryDate())
                .cancelledAt(sub.getCancelledAt())
                .daysRemaining(daysRemaining)
                .build();
    }
}
