package dev.anuradha.fcmembership.repository;

import dev.anuradha.fcmembership.entity.MembershipTier;
import dev.anuradha.fcmembership.enums.TierType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipTierRepository extends JpaRepository<MembershipTier, Long> {

    List<MembershipTier> findByPlanIdAndActiveTrueOrderByTierLevelAsc(Long planId);

    Optional<MembershipTier> findByPlanIdAndTierType(Long planId, TierType tierType);

    // Fetch tiers with their criteria eagerly — used by tier evaluation engine
    @Query("SELECT t FROM MembershipTier t LEFT JOIN FETCH t.criteriaList WHERE t.plan.id = :planId AND t.active = true ORDER BY t.tierLevel DESC")
    List<MembershipTier> findTiersWithCriteriaByPlanId(Long planId);
}
