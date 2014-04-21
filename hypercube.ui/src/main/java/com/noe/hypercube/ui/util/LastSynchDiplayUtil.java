package com.noe.hypercube.ui.util;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public final class LastSynchDiplayUtil {

    public static final String DATE_PATTERN = "yyyy.MM.dd";
    private static final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern(DATE_PATTERN);

    private LastSynchDiplayUtil() {
    }

    public static String convertToString(Date lastSynchDate) {
        DateTime now = new DateTime();
        DateTime lastSynchDateTime = new DateTime(lastSynchDate);
        final Period period = new Period(lastSynchDateTime, now);

        String result = timeFormatter.print(lastSynchDateTime.getMillis());
        int elapsedTime = period.getMonths();
        if(elapsedTime == 0) {
            int weeks = period.getWeeks();
            elapsedTime = period.getDays();
            if(elapsedTime == 0 && weeks == 0) {
                elapsedTime = period.getHours();
                if(elapsedTime == 0) {
                    elapsedTime = period.getMinutes();
                    if (elapsedTime == 0) {
                        elapsedTime = period.getSeconds();
                        if (elapsedTime == 0) {
                            result = "moments ago";
                        }
                        else {
                            result = elapsedTime + " second(s) ago";
                        }
                    }
                    else {
                        result = elapsedTime + " minute(s) ago";
                    }
                }
                else {
                    result = elapsedTime + " hour(s) ago";
                }
            }
            else {
                result = weeks * 7 + elapsedTime + " day(s) ago";
            }
        }
        return result;
    }
}
