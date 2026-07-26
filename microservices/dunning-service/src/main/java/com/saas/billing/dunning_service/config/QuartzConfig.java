package com.saas.billing.dunning_service.config;

import com.saas.billing.dunning_service.scheduler.DunningRetryJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail dunningRetryJobDetail() {
        return JobBuilder.newJob(DunningRetryJob.class)
                .withIdentity("dunningRetryJob")
                .withDescription(
                        "Execute scheduled dunning retries"
                )
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger dunningRetryTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(dunningRetryJobDetail())
                .withIdentity("dunningRetryTrigger")
                .withSchedule(
                        // chaque nuit à 3h00
                        // après billing (2h00)
                        // et downgrade (2h05)
                        CronScheduleBuilder.cronSchedule(
                                "0 0 3 * * ?"
                        )
                )
                .build();
    }
}