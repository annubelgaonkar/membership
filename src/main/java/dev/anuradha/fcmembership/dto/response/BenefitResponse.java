package dev.anuradha.fcmembership.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BenefitResponse {

    private Long id;
    private String benefitType;
    private String description;
    private BigDecimal discountPercentage;
    private Boolean freeDelivery;
    private Boolean featureEnabled;
}
