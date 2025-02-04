package com.skillbox.cryptobot.sheduler;

import com.skillbox.cryptobot.bot.CryptoBot;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import com.skillbox.cryptobot.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class BitcoinPriceScheduler {

    private final CryptoCurrencyService cryptoCurrencyService;
    private final SubscriptionService subscriptionService;
    private final CryptoBot cryptoBot;

    public BitcoinPriceScheduler(
            CryptoCurrencyService cryptoCurrencyService,
            SubscriptionService subscriptionService,
            CryptoBot bot
    ) {
        this.cryptoCurrencyService = cryptoCurrencyService;
        this.subscriptionService = subscriptionService;
        this.cryptoBot = bot;
    }

    @Scheduled(fixedRate = 120000) // Запуск каждые 2 минуты
    public void checkBitcoinPrice() {
        log.info("Запуск проверки цены биткоина...");
        try {
            double currentPrice = cryptoCurrencyService.getBitcoinPrice();
            log.info("Текущая цена биткоина: " + currentPrice);

            // Получаем пользователей, у которых подписка выше текущей цены
            List<Long> usersToNotify = subscriptionService.getUsersWithSubscriptionAbovePrice(currentPrice);

            // Отправляем уведомления
            for (Long userId : usersToNotify) {
                if (subscriptionService.canSendNotification(userId)) {
                    String message = "Пора покупать, стоимость биткоина " + currentPrice + " USD";
                    cryptoBot.sendNotification(userId, message);
                    subscriptionService.updateLastNotificationTime(userId);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при проверке цены биткоина", e);
        }
    }
}