package dev.anuradha.fcmembership.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.anuradha.fcmembership.enums.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionResponse {

    private Long subscriptionId;
    private Long userId;
    private String userName;

    private Long planId;
    private String planName;
    private BigDecimal planPrice;
    //current tier only
    private TierResponse tier;
    private String tierChange;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private LocalDateTime cancelledAt;
    private long daysRemaining;
}
