package dev.anuradha.fcmembership.tier.criteria;

import dev.anuradha.fcmembership.entity.TierCriteria;
import dev.anuradha.fcmembership.entity.User;
import dev.anuradha.fcmembership.enums.CriteriaType;
import org.springframework.stereotype.Component;

@Component
public class MinOrderValueEvaluator implements TierCriteriaEvaluator{

    @Override
    public CriteriaType supports(){
        return CriteriaType.MIN_ORDER_VALUE;
    }

    @Override
    public boolean evaluate (User user, TierCriteria criteria){
        if(criteria.getMinOrderValue() == null)     return false;
        return user.getTotalOrderValueThisMonth()
                .compareTo(criteria.getMinOrderValue()) >= 0;
    }
}
