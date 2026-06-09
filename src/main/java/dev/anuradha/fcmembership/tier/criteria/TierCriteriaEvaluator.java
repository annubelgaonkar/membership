package dev.anuradha.fcmembership.tier.criteria;

import dev.anuradha.fcmembership.entity.TierCriteria;
import dev.anuradha.fcmembership.entity.User;
import dev.anuradha.fcmembership.enums.CriteriaType;

public interface TierCriteriaEvaluator {

    CriteriaType supports();
    boolean evaluate (User user, TierCriteria criteria);


}
