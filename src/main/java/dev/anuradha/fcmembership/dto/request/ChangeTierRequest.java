package dev.anuradha.fcmembership.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeTierRequest {

    @NotNull(message = "User Id is required")
    private Long userId;

    @NotNull(message = "New Tier Id is required")
    private Long newTierId;


}
