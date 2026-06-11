package dev.anuradha.fcmembership.controller;

import dev.anuradha.fcmembership.dto.request.ChangeTierRequest;
import dev.anuradha.fcmembership.dto.request.SubscribeRequest;
import dev.anuradha.fcmembership.dto.response.ApiResponse;
import dev.anuradha.fcmembership.dto.response.PlanResponse;
import dev.anuradha.fcmembership.dto.response.SubscriptionResponse;
import dev.anuradha.fcmembership.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getAllPPlans(){
        List<PlanResponse> plans = membershipService.getAllPlans();
        return ResponseEntity.ok(ApiResponse.success("Available membership plans",
                plans));

    }

    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribe(
            @Valid @RequestBody SubscribeRequest request){
        SubscriptionResponse response = membershipService.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscribed successfully", response));
    }

    // Admin/Support only — manual tier override bypassing criteria validation.
    // For criteria-based tier assignment, use PUT /evaluate-tier/{userId} API
    @PutMapping("/tier")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> changeTier(
            @Valid @RequestBody ChangeTierRequest request){
        SubscriptionResponse response = membershipService.changeTier(request);
        return ResponseEntity.ok(ApiResponse.success("Tier changed successfully",
                response));

    }

    @PutMapping("/cancel/{userId}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancel(
            @PathVariable Long userId){
        SubscriptionResponse response = membershipService.cancelSubscription(userId);
        return ResponseEntity.ok(ApiResponse.success("Success! Subscription cancelled",
                response));

    }

    // GET SUBSCRIPTION STATUS
    @GetMapping("/status/{userId}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getStatus(
            @PathVariable Long userId){
        SubscriptionResponse subscription = membershipService.getSubscription(userId);
        return ResponseEntity.ok(ApiResponse.success(subscription));
    }

    @PutMapping("evaluate-tier/{userId}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> evaluateTier(
            @PathVariable Long userId){
        SubscriptionResponse response = membershipService.evaluateAndUpgradeTier(userId);
        return ResponseEntity.ok(ApiResponse.success("Tier evaluated successfully",
                response));
    }

}
