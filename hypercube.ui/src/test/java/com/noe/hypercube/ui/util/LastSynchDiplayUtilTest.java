package com.noe.hypercube.ui.util;

import org.hamcrest.CoreMatchers;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class LastSynchDiplayUtilTest {

    @Test
    public void shouldReturnMomentsAgoWhenElapsedTimeIsZero() throws Exception {
        String result = LastSynchDiplayUtil.convertToString(new Date());
        Assert.assertThat(result, CoreMatchers.equalTo("moments ago"));
    }

    @Test
    public void shouldReturnSecondPrecisionWhenElapsedTimeIsBetweenZeroAndOneMinute() throws Exception {
        int secondsAgo = 59;
        Date lastSynchDate = new DateTime().minusSeconds(secondsAgo).toDate();
        String result = LastSynchDiplayUtil.convertToString(lastSynchDate);
        Assert.assertThat(result, CoreMatchers.equalTo(secondsAgo + " second(s) ago"));
    }

    @Test
    public void shouldReturnMinutePrecisionWhenElapsedTimeIsBetweenOneMinuteAndAnHour() throws Exception {
        int minutesAgo = 59;
        Date lastSynchDate = new DateTime().minusMinutes(minutesAgo).toDate();
        String result = LastSynchDiplayUtil.convertToString(lastSynchDate);
        Assert.assertThat(result, CoreMatchers.equalTo(minutesAgo + " minute(s) ago"));
    }

    @Test
    public void shouldReturnHourPrecisionWhenElapsedTimeIsBetweenOneHourAndADay() throws Exception {
        int minutesAgo = 59;
        int hoursAgo = 23;
        Date lastSynchDate = new DateTime().minusHours(hoursAgo).minusMinutes(minutesAgo).toDate();
        String result = LastSynchDiplayUtil.convertToString(lastSynchDate);
        Assert.assertThat(result, CoreMatchers.equalTo(hoursAgo + " hour(s) ago"));
    }

    @Test
    public void shouldReturnDayPrecisionWhenElapsedTimeIsBetweenOneDayAndThirtyDays() throws Exception {
        int minutesAgo = 59;
        int hoursAgo = 23;
        int daysAgo = 29;
        Date lastSynchDate = new DateTime().minusDays(daysAgo).minusHours(hoursAgo).minusMinutes(minutesAgo).toDate();
        String result = LastSynchDiplayUtil.convertToString(lastSynchDate);
        Assert.assertThat(result, CoreMatchers.equalTo(daysAgo + " day(s) ago"));
    }

    @Test
    public void shouldReturnFormattedDateWhenElapsedTimeIsMoreThenThirtyDays() throws Exception {
        int minutesAgo = 59;
        int hoursAgo = 23;
        int daysAgo = 42;
        DateTime lastSynchDate = new DateTime(2000,1,1,1,1).minusDays(daysAgo).minusHours(hoursAgo).minusMinutes(minutesAgo);
        String result = LastSynchDiplayUtil.convertToString(lastSynchDate.toDate());
        Assert.assertThat(result, CoreMatchers.equalTo("1999.11.19"));
    }
}
