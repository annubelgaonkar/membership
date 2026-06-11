package dev.anuradha.fcmembership.repository;

import dev.anuradha.fcmembership.entity.UserSubscription;
import dev.anuradha.fcmembership.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    //find Active subscription for a user
    Optional<UserSubscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);

    // To check if user already has any active/non-expired subscription
    Optional<UserSubscription> findByUserIdAndStatusAndExpiryDateAfter(
            Long userId,
            SubscriptionStatus status,
            LocalDateTime now
    );

    List<UserSubscription> findByUserIdOrderByCreatedAtDesc(Long userId);

    // For expiry jobs (future extensibility)
    @Query("SELECT s FROM UserSubscription s WHERE s.status = 'ACTIVE' AND s.expiryDate < :now")
    List<UserSubscription> findExpiredSubscriptions(LocalDateTime now);
}
