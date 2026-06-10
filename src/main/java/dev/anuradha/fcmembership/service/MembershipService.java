package dev.anuradha.fcmembership.service;

import dev.anuradha.fcmembership.dto.request.ChangeTierRequest;
import dev.anuradha.fcmembership.dto.request.SubscribeRequest;
import dev.anuradha.fcmembership.dto.response.PlanResponse;
import dev.anuradha.fcmembership.dto.response.SubscriptionResponse;

import java.util.List;

public interface MembershipService {

    //get all active plans with their tiers
    List<PlanResponse> getAllPlans();

    //A user subscribing  to a plan + tier
    SubscriptionResponse subscribe(SubscribeRequest request);

    //User can upgrade/downgrade
    SubscriptionResponse changeTier(ChangeTierRequest request);

    //user can cancel subscription
    SubscriptionResponse cancelSubscription(Long userId);

    //get current active subscription for the user
    SubscriptionResponse getSubscription(Long userId);

    //Re-evaluate the user's order history and auto assign best elegible tier
    SubscriptionResponse evaluateAndUpgradeTier(Long userId);


}
