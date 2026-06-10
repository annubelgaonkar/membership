package dev.anuradha.fcmembership.repository;

import dev.anuradha.fcmembership.entity.MembershipPlan;
import dev.anuradha.fcmembership.enums.PlanDuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {

    List<MembershipPlan> findByActiveTrue();
    Optional<MembershipPlan> findByDurationAndActiveTrue(PlanDuration duration);

}
