package com.skillbox.cryptobot.service;

import com.skillbox.cryptobot.bot.CryptoBot;
import com.skillbox.cryptobot.entity.Subscriber;
import com.skillbox.cryptobot.repository.SubscriberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j

public class SubscriptionService {

    private final SubscriberRepository subscriberRepository;
    private final Map<Long, Long> lastNotificationTime = new HashMap<>();

    @Autowired
    @Lazy
    private CryptoBot bot;

    public SubscriptionService(SubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }

    public Double getSubscriptionPrice(Long telegramUserId) {
        return subscriberRepository.findByTelegramUserId(telegramUserId)
                .map(Subscriber::getSubscriptionPrice)
                .orElse(null);
    }

    public List<Long> getUsersWithSubscriptionAbovePrice(double currentPrice) {
        return subscriberRepository.findBySubscriptionPriceGreaterThan(currentPrice)
                .stream()
                .map(Subscriber::getTelegramUserId)
                .collect(Collectors.toList());
    }

    public boolean canSendNotification(Long telegramUserId) {
        long currentTime = System.currentTimeMillis();
        long lastTime = lastNotificationTime.getOrDefault(telegramUserId, 0L);
        return (currentTime - lastTime) >= 600000; // 10 минут
    }

    public void sendNotification(Long telegramUserId, double currentPrice) {
        String message = "Пора покупать, стоимость биткоина " + currentPrice + " USD";

        try {
            bot.sendNotification(telegramUserId, message);
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления пользователю " + telegramUserId, e);
        }
    }

    public void updateLastNotificationTime(Long telegramUserId) {
        lastNotificationTime.put(telegramUserId, System.currentTimeMillis());
    }

    public void updateSubscription(Long telegramUserId, double price) {
        Subscriber subscriber = subscriberRepository.findByTelegramUserId(telegramUserId)
                .orElseGet(() -> {
                    Subscriber newSubscriber = new Subscriber();
                    newSubscriber.setTelegramUserId(telegramUserId);
                    return newSubscriber;
                });

        subscriber.setSubscriptionPrice(price);
        subscriberRepository.save(subscriber);
    }

    public void removeSubscription(Long telegramUserId) {
        subscriberRepository.findByTelegramUserId(telegramUserId)
                .ifPresent(subscriber -> {
                    subscriber.setSubscriptionPrice(null);
                    subscriberRepository.save(subscriber);
                });
    }

    public void addUserToDatabase(Long telegramUserId) {
        if (!subscriberRepository.existsByTelegramUserId(telegramUserId)) {
            Subscriber subscriber = new Subscriber();
            subscriber.setTelegramUserId(telegramUserId);
            subscriberRepository.save(subscriber);
        }
    }

    public boolean userExistsInDatabase(Long telegramUserId) {
        return subscriberRepository.existsByTelegramUserId(telegramUserId);
    }
}