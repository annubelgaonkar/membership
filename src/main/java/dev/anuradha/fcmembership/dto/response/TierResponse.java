package dev.anuradha.fcmembership.dto.response;

import dev.anuradha.fcmembership.enums.TierType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TierResponse {

    private Long id;
    private TierType tierType;
    private String name;
    private String description;
    private Integer tierLevel;
    private List<BenefitResponse> benefits;
    private List<CriteriaResponse> criteria;

}
