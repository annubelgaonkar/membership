package dev.anuradha.fcmembership.tier;

import dev.anuradha.fcmembership.entity.MembershipTier;
import dev.anuradha.fcmembership.entity.TierCriteria;
import dev.anuradha.fcmembership.entity.User;
import dev.anuradha.fcmembership.enums.CriteriaType;
import dev.anuradha.fcmembership.repository.MembershipTierRepository;
import dev.anuradha.fcmembership.tier.criteria.TierCriteriaEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TierEvaluationEngine {

    private final Map<CriteriaType, TierCriteriaEvaluator> evaluatorMap;
    private final MembershipTierRepository tierRepository;

    public TierEvaluationEngine(List<TierCriteriaEvaluator> evaluators,
                                MembershipTierRepository tierRepository){

        this.tierRepository = tierRepository;
        this.evaluatorMap = evaluators.stream()
                .collect(Collectors.toMap(
                        TierCriteriaEvaluator::supports,
                        Function.identity()
                ));
        log.info("TierEValuationEngine intialized with evaluators: {}",
                evaluatorMap.keySet());
    }
    public Optional<MembershipTier> evaluateBestTier(User user, Long planId){
        List<MembershipTier> tiers =
                tierRepository.findTiersWithCriteriaByPlanId(planId);

        return tiers.stream()
                .filter(tier -> qualifiesForTier(user, tier))
                .findFirst();
    }

    private boolean qualifiesForTier(User user, MembershipTier tier){
        List<TierCriteria> criteriaList = tier.getCriteriaList();
        if(criteriaList == null || criteriaList.isEmpty()){
            log.warn("Tier has no criteria configured", tier.getName());
            return false;
        }

        return criteriaList.stream().allMatch(criteria -> {
            TierCriteriaEvaluator evaluator = evaluatorMap.get(criteria.getCriteriaType());
            if(evaluator == null){
                log.warn("No evaluator found for criteria type: {}", criteria.getCriteriaType());
                return false;
            }
            boolean result = evaluator.evaluate(user, criteria);
            log.debug("User {} | criteria {} | Result: {}",
                    user.getEmail(), criteria.getCriteriaType(), result);
            return result;
        });
    }
}
