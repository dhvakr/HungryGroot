package me.dhvakr.config;

import me.dhvakr.jpa.service.GrootService;
import lombok.extern.slf4j.Slf4j;
import me.dhvakr.constants.Constants;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Class to schedule to run cleanUp action to clear records of food history count that
 * exceeds more than 4 days,
 * </p>
 * Refer {@link org.springframework.scheduling.annotation.SchedulingConfiguration} more information
 */
@Slf4j
@Configuration
@EnableScheduling
public class SchedulingConfiguration {

    //~ Static fields/initializers =========================================================================================================

    private static final String LOG_TAG = Constants.DEFAULT_LOG_TAG + "SCHEDULER ";

    //~ Instance fields ====================================================================================================================

    private GrootService grootService;

    //~ Constructor ========================================================================================================================

    SchedulingConfiguration(GrootService grootService) {
        log.info(LOG_TAG.trim() + "-UPTIME::" + LocalTime.now(ZoneId.systemDefault()));
        this.grootService = grootService;
    }

    //~ Methods ============================================================================================================================

    /**
     * <p> This method will be executed at 12:00:00 AM of every day at midnight and deletes
     * the foodCountData rows older than 4 days </p>
     *
     * This cron job syntax ( * * * * * ), is indicated by the following:
     * @apiNote  Seconds, Minutes (0 - 59), Hours (0 - 23), Day of month (1 - 31), Month (1 - 12), Day of week (0 - 6)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupOldFoodCountHistory() {
        log.info(LOG_TAG + "::Started FoodCountHistory cleanUp job:: " + LocalTime.now(ZoneId.systemDefault()));
        LocalDate fourDaysAgo = LocalDate.now().minusDays(4);
        grootService.deleteByFoodCountDateBefore(fourDaysAgo);
        log.info(LOG_TAG + "::Completed FoodCountHistory cleanup job:: " + LocalTime.now(ZoneId.systemDefault()));
    }
}
