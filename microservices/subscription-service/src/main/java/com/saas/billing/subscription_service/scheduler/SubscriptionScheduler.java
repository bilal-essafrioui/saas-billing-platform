package com.saas.billing.subscription_service.scheduler;


import com.saas.billing.subscription_service.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {
    private final SubscriptionService subscriptionService;

    @Scheduled(fixedRate = 300000) // every 5 minutes
    public void processDailyRenewals(){
        System.out.print("HELLO FROM SPRING SCHEDULER");
        subscriptionService.processDailyRenewals();
        System.out.print("Good BYE FROM SPRING SCHEDULER");
    }
}
