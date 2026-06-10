package dev.anuradha.fcmembership.dto.response;

import dev.anuradha.fcmembership.enums.PlanDuration;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PlanResponse {
    private Long id;
    private String name;
    private PlanDuration duration;
    private BigDecimal price;
    private Integer durationInDays;
    private String description;
    private List<TierResponse> tiers;
}
