package dev.anuradha.fcmembership.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscribeRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Plan id is required")
    private Long planId;

    @NotNull(message = "Tier ID is required")
    private Long tierId;

}
