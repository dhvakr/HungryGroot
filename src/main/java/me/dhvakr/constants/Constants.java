package me.dhvakr.constants;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class Constants {

    //~ Private Constructor ================================================================================================================

    private Constants(){}

    //~ CONSTANTS ==========================================================================================================================

    /**
     *  Common
     */
    public static final String DEFAULT_LOG_TAG = "GROOT::";
    public static final String COMPANY_NAME = "Hungry Groot";
    public static final String ROLE_PREFIX = "ROLE_";

    /**
     * Env Variables
     */
    public static final int RECURRING_START_TIME = Integer.parseInt("RECURRING_START_TIME");
    public static final int RECURRING_END_TIME_TEXT = Integer.parseInt("RECURRING_END_TIME_TEXT");

    /**
     * Notification Message
     */
    public static final String NOTIFY_SUCCESS_RECORD_COUNT_MESSAGE = "Your count is recorded ";
    public static final String NOTIFY_FAILURE_RECORD_COUNT_MESSAGE = "Your count is removed ";
    public static final String NOTIFY_RECORD_COUNT_MESSAGE = "Your count is already updated ";

    /**
     * Email Count Data
     */
    public static final String FROM = "people@dhvakr.me";
    public static final String EMAIL_SUBJECT = "Food Count - Status for " + LocalDate.now();
    public static final String SUCCESS_EMAIL_BODY = """
            Hi there,
            
            Total count is 
            """.formatted();
}


