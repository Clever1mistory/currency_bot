package com.skillbox.cryptobot.repository;

import com.skillbox.cryptobot.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriberRepository extends JpaRepository<Subscriber, UUID> {
    Optional<Subscriber> findByTelegramUserId(Long telegramUserId);
    boolean existsByTelegramUserId(Long telegramUserId);
    List<Subscriber> findBySubscriptionPriceGreaterThan(Double price);
}
