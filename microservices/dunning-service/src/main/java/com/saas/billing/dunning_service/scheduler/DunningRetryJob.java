package com.saas.billing.dunning_service.scheduler;

import com.saas.billing.dunning_service.service.DunningService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DunningRetryJob implements Job {

    @Autowired
    private DunningService dunningService;

    // ════════════════════════════════════
    // JOB QUARTZ
    // tourne chaque nuit à 3h00
    // (après le billing job à 2h00)
    //
    // trouve toutes les relances
    // dont scheduled_date = aujourd'hui
    // et status = SCHEDULED
    // les exécute une par une
    // ════════════════════════════════════

    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        System.out.println(
                "DunningRetryJob started : " + LocalDate.now()
        );

        dunningService.executeScheduledRetries();

        System.out.println("DunningRetryJob completed");
    }
}