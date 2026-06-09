package dev.anuradha.fcmembership.tier.criteria;

import dev.anuradha.fcmembership.enums.CriteriaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class TierEvaluationEngine {

    private final Map<CriteriaType, TierCriteriaEvaluator> evaluatorMap;

}
