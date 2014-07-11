package com.noe.hypercube.ui.desktop.util;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public final class DateUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern( "yyyy.MM.dd  HH:mm:ss" );;

    private DateUtil() {
    }

    public static String format(Date date) {
        return format(date.getTime());
    }

    public static String format(Long time) {
        return DATE_TIME_FORMATTER.print(time);
    }
}
