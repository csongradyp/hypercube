package com.noe.hypercube.ui.util;

import org.hamcrest.CoreMatchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.assertThat;

public class LastSyncDisplayUtilTest {

    private ResourceBundle bundle;

    @Before
    public void setUp() {
        bundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
    }

    @Test
    public void shouldReturnMomentsAgoWhenElapsedTimeIsZero() throws Exception {
        String result = LastSyncDisplayUtil.convertToString(new Date(), bundle);
        assertThat(result, CoreMatchers.equalTo("moments ago"));
    }

    @Test
    public void shouldReturnSecondPrecisionWhenElapsedTimeIsBetweenZeroAndOneMinute() throws Exception {
        int secondsAgo = 59;
        Date lastSyncDate = new DateTime().minusSeconds(secondsAgo).toDate();
        String result = LastSyncDisplayUtil.convertToString(lastSyncDate, bundle);
        assertThat(result, CoreMatchers.equalTo(secondsAgo + " sec. ago"));
    }

    @Test
    public void shouldReturnMinutePrecisionWhenElapsedTimeIsBetweenOneMinuteAndAnHour() throws Exception {
        int minutesAgo = 59;
        Date lastSyncDate = new DateTime().minusMinutes(minutesAgo).toDate();
        String result = LastSyncDisplayUtil.convertToString(lastSyncDate, bundle);
        assertThat(result, CoreMatchers.equalTo(minutesAgo + " min. ago"));
    }

    @Test
    public void shouldReturnHourPrecisionWhenElapsedTimeIsBetweenOneHourAndADay() throws Exception {
        int minutesAgo = 59;
        int hoursAgo = 23;
        Date lastSyncDate = new DateTime().minusHours(hoursAgo).minusMinutes(minutesAgo).toDate();
        String result = LastSyncDisplayUtil.convertToString(lastSyncDate, bundle);
        assertThat(result, CoreMatchers.equalTo(hoursAgo + " hour(s) ago"));
    }

    @Test
    public void shouldReturnDayPrecisionWhenElapsedTimeIsBetweenOneDayAndThirtyDays() throws Exception {
        int minutesAgo = 59;
        int hoursAgo = 23;
        int daysAgo = 29;
        Date lastSyncDate = new DateTime().minusDays(daysAgo).minusHours(hoursAgo).minusMinutes(minutesAgo).toDate();
        String result = LastSyncDisplayUtil.convertToString(lastSyncDate, bundle);
        assertThat(result, CoreMatchers.equalTo(daysAgo + " day(s) ago"));
    }

    @Test
    public void shouldReturnFormattedDateWhenElapsedTimeIsMoreThenThirtyDays() throws Exception {
        int minutesAgo = 59;
        int hoursAgo = 23;
        int daysAgo = 42;
        DateTime lastSyncDate = new DateTime(2000, 1, 1, 1, 1).minusDays(daysAgo).minusHours(hoursAgo).minusMinutes(minutesAgo);
        String result = LastSyncDisplayUtil.convertToString(lastSyncDate.toDate(), bundle);
        assertThat(result, CoreMatchers.equalTo("1999.11.19"));
    }
}
