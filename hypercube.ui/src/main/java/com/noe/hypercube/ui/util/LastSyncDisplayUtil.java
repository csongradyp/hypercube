package com.noe.hypercube.ui.util;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.ResourceBundle;

public final class LastSyncDisplayUtil {

    public static final String DATE_PATTERN = "yyyy.MM.dd";
    private static final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern(DATE_PATTERN);

    private LastSyncDisplayUtil() {
    }

    public static String convertToString(final long lastSyncDate, final ResourceBundle messageBundle) {
        return convertToString(new Date(lastSyncDate), messageBundle);
    }

    public static String convertToString(final Date lastSyncDate, final ResourceBundle messageBundle) {
        DateTime now = new DateTime();
        DateTime lastSyncDateTime = new DateTime(lastSyncDate);
        final Period period = new Period(lastSyncDateTime, now);

        String result = timeFormatter.print(lastSyncDateTime.getMillis());
        int elapsedTime = period.getMonths();
        if (elapsedTime == 0) {
            final int weeks = period.getWeeks();
            elapsedTime = period.getDays();
            if (elapsedTime == 0 && weeks == 0) {
                elapsedTime = period.getHours();
                if (elapsedTime == 0) {
                    elapsedTime = period.getMinutes();
                    if (elapsedTime == 0) {
                        elapsedTime = period.getSeconds();
                        if (elapsedTime == 0) {
                            result = messageBundle.getString("sync.moments");
                        } else {
                            result = elapsedTime + messageBundle.getString("sync.seconds");
                        }
                    } else {
                        result = elapsedTime + messageBundle.getString("sync.minutes");
                    }
                } else {
                    result = elapsedTime + messageBundle.getString("sync.hours");
                }
            } else {
                result = weeks * 7 + elapsedTime + messageBundle.getString("sync.days");
            }
        }
        return result;
    }
}
