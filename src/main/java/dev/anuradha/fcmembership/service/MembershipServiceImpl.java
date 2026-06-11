package dev.anuradha.fcmembership.service;

import dev.anuradha.fcmembership.dto.request.ChangeTierRequest;
import dev.anuradha.fcmembership.dto.request.SubscribeRequest;
import dev.anuradha.fcmembership.dto.response.PlanResponse;
import dev.anuradha.fcmembership.dto.response.SubscriptionResponse;
import dev.anuradha.fcmembership.entity.MembershipPlan;
import dev.anuradha.fcmembership.entity.MembershipTier;
import dev.anuradha.fcmembership.entity.User;
import dev.anuradha.fcmembership.entity.UserSubscription;
import dev.anuradha.fcmembership.enums.SubscriptionStatus;
import dev.anuradha.fcmembership.enums.TierType;
import dev.anuradha.fcmembership.exception.ResourceNotFoundException;
import dev.anuradha.fcmembership.exception.SubscriptionException;
import dev.anuradha.fcmembership.exception.TierMismatchException;
import dev.anuradha.fcmembership.mapper.MembershipMapper;
import dev.anuradha.fcmembership.repository.MembershipPlanRepository;
import dev.anuradha.fcmembership.repository.MembershipTierRepository;
import dev.anuradha.fcmembership.repository.UserRepository;
import dev.anuradha.fcmembership.repository.UserSubscriptionRepository;
import dev.anuradha.fcmembership.tier.TierEvaluationEngine;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService{

    private final MembershipPlanRepository planRepository;
    private final MembershipTierRepository tierRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final MembershipMapper mapper;
    private final TierEvaluationEngine tierEvaluationEngine;

    @Override
    public List<PlanResponse> getAllPlans() {
        return planRepository.findByActiveTrue()
                .stream()
                .map(mapper::toPlanResponse)
                .toList();
    }

    @Override
    @Transactional
    public SubscriptionResponse subscribe(SubscribeRequest request) {

        User user = findUserById(request.getUserId());
        MembershipPlan plan = findPlanById(request.getPlanId());

        // Check if user already has an active subscription
        subscriptionRepository.findByUserIdAndStatusAndExpiryDateAfter(
                        user.getId(), SubscriptionStatus.ACTIVE, LocalDateTime.now()
                )
                .ifPresent(existing -> {
                    throw new SubscriptionException(
                            "User already has an active subscription. Please cancel it before subscribing to a new plan."
                    );
                });

        // Always start at Silver — tier is upgraded later via evaluate-tier
        MembershipTier silverTier = tierRepository
                .findByPlanIdAndTierType(plan.getId(), TierType.SILVER)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Silver tier not found for plan: " + plan.getName()
                ));

        LocalDateTime now = LocalDateTime.now();
        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .plan(plan)
                .tier(silverTier)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(now)
                .expiryDate(now.plusDays(plan.getDurationInDays()))
                .build();

        UserSubscription saved = subscriptionRepository.save(subscription);
        log.info("User {} subscribed to plan {} — starting tier: Silver",
                user.getEmail(), plan.getName());

        return mapper.toSubscriptionResponse(saved);
    }

    //change tier i.e. upgrade/downgrade
    @Override
    @Transactional
    public SubscriptionResponse changeTier(ChangeTierRequest request){
        // @Version on UserSubscription handles concurrent modification
        UserSubscription subscription = findActiveSubscription(request.getUserId());
        MembershipTier newTier = findTierById(request.getNewTierId());

        if(!newTier.getPlan().getId().equals(subscription.getPlan().getId())){
            throw new TierMismatchException(
                    "New tier '" + newTier.getName() + "' does not belong to current plan '"
                            + subscription.getPlan().getName() + "'. To change plan, cancel and re-subscribe."
            );
        }

        MembershipTier currentTier = subscription.getTier();

        if(currentTier.getId().equals(newTier.getId())){
            throw new SubscriptionException("You are already on the "+currentTier.getName() + " tier");
        }

        String changeType = newTier.getTierLevel() > currentTier.getTierLevel()
                ? "upgraded" : "downgraded";

        subscription.setTier(newTier);
        UserSubscription saved = subscriptionRepository.save(subscription);
        log.info("user {} {} from {} to {}",
                subscription.getUser().getEmail(),changeType,
                currentTier.getName(), newTier.getName());

        return mapper.toSubscriptionResponse(saved);
    }


    //cancellation
    @Override
    @Transactional
    public SubscriptionResponse cancelSubscription(Long userId) {

        UserSubscription subscription = findActiveSubscription(userId);

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());

        UserSubscription saved = subscriptionRepository.save(subscription);
        log.info("User ID {} cancelled subscription ID {}", userId, saved.getId());

        return mapper.toSubscriptionResponse(saved);
    }

    //get subscription status
    @Override
    @Transactional
    public SubscriptionResponse getSubscription(Long userId) {
        findUserById(userId);

        UserSubscription subscription = subscriptionRepository.findByUserIdAndStatusAndExpiryDateAfter(
                        userId, SubscriptionStatus.ACTIVE, LocalDateTime.now()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active subscription found for user ID: " + userId
                ));
        return mapper.toSubscriptionResponse(subscription);
    }

    // ── EVALUATE AND AUTO-ASSIGN BEST TIER
    @Override
    @Transactional
    public SubscriptionResponse evaluateAndUpgradeTier(Long userId) {
        UserSubscription subscription = findActiveSubscription(userId);
        User user = subscription.getUser();
        Long planId = subscription.getPlan().getId();

        MembershipTier bestTier = tierEvaluationEngine
                .evaluateBestTier(user, planId)
                .orElseThrow(() -> new SubscriptionException(
                        "User does not qualify for any tier in the current plan"
                ));

        if (bestTier.getId().equals(subscription.getTier().getId())) {
            log.info("User {} already on best qualifying tier: {}", user.getEmail(), bestTier.getName());
        } else {
            log.info("User {} tier evaluated: {} → {}",
                    user.getEmail(), subscription.getTier().getName(), bestTier.getName());
            subscription.setTier(bestTier);
            subscriptionRepository.save(subscription);
        }

        return mapper.toSubscriptionResponse(subscription);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + userId
                ));
    }
    private MembershipPlan findPlanById(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Membership plan not found with ID: " + planId
                ));
    }

    private MembershipTier findTierById(Long tierId) {
        return tierRepository.findById(tierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Membership tier not found with ID: " + tierId
                ));
    }
    private UserSubscription findActiveSubscription(Long userId) {
        return subscriptionRepository.findByUserIdAndStatusAndExpiryDateAfter(
                        userId, SubscriptionStatus.ACTIVE, LocalDateTime.now()
                )
                .orElseThrow(() -> new SubscriptionException(
                        "No active subscription found for user ID: " + userId
                ));
    }
}
