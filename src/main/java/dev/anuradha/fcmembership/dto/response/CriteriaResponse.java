package dev.anuradha.fcmembership.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.anuradha.fcmembership.enums.CriteriaType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CriteriaResponse {
    private Long id;
    private CriteriaType criteriaType;
    private Integer minOrderCount;
    private BigDecimal minOrderValue;
    private String cohortName;
}
