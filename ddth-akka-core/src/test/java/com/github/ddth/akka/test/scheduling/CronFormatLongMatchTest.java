package com.github.ddth.akka.test.scheduling;

import java.util.Calendar;
import java.util.Date;

import com.github.ddth.akka.scheduling.CronFormat;
import com.github.ddth.commons.utils.DateFormatUtils;
import com.github.ddth.commons.utils.DateTimeUtils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CronFormatLongMatchTest extends TestCase {

    public CronFormatLongMatchTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(CronFormatLongMatchTest.class);
    }

    public static Throwable runException(Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            return t;
        }
        return null;
    }

    public void testAnyMoment() {
        CronFormat cf = CronFormat.parse("* * * * * *");
        assertNotNull(cf);
        assertTrue(cf.matches(System.currentTimeMillis()));
        assertTrue(cf.matches(new Date()));
        assertTrue(cf.matches(Calendar.getInstance()));
    }

    public void testEvery3Seconds() throws InterruptedException {
        CronFormat cf = CronFormat.parse("*/3 * * * * *");
        assertNotNull(cf);
        Calendar cal = Calendar.getInstance();
        int matches = 0;
        for (int i = 0; i < 3 * 2; i++) {
            cal.set(Calendar.SECOND, 1 + i);
            matches += cf.matches(cal) ? 1 : 0;
        }
        assertEquals(2, matches);
    }

    public void testEvery3Minutes() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* */3 * * * *");
        assertNotNull(cf);
        Calendar cal = Calendar.getInstance();
        int matches = 0;
        for (int i = 0; i < 3 * 2; i++) {
            cal.set(Calendar.MINUTE, 1 + i);
            matches += cf.matches(cal) ? 1 : 0;
        }
        assertEquals(2, matches);
    }

    public void testEvery3Hours() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* * */3 * * *");
        assertNotNull(cf);
        Calendar cal = Calendar.getInstance();
        int matches = 0;
        for (int i = 0; i < 3 * 2; i++) {
            cal.set(Calendar.HOUR, 1 + i);
            matches += cf.matches(cal) ? 1 : 0;
        }
        assertEquals(2, matches);
    }

    public void testEvery3Days() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* * * */3 * *");
        assertNotNull(cf);
        Calendar cal = Calendar.getInstance();
        int matches = 0;
        for (int i = 0; i < 3 * 2; i++) {
            cal.set(Calendar.DAY_OF_MONTH, 1 + i);
            matches += cf.matches(cal) ? 1 : 0;
        }
        assertEquals(2, matches);
    }

    public void testEvery3Months() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* * * * */3 *");
        assertNotNull(cf);
        Calendar cal = Calendar.getInstance();
        int matches = 0;
        for (int i = 0; i < 3 * 2; i++) {
            cal.set(Calendar.MONTH, Calendar.JANUARY + i);
            matches += cf.matches(cal) ? 1 : 0;
        }
        assertEquals(2, matches);
    }

    public void testEvery3Dows() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* * * * * */3");
        assertNotNull(cf);
        Calendar cal = Calendar.getInstance();
        int matches = 0;
        for (int i = 0; i < 3 * 2; i++) {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY + i);
            matches += cf.matches(cal) ? 1 : 0;
        }
        assertEquals(2, matches);
    }

    public void testMultiValuesSecond() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("1,3,5,7 * * 1 1 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 12:34:00", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-01 12:34:01", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 12:34:02", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-01 12:34:03", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 12:34:04", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-01 12:34:05", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 12:34:06", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-01 12:34:07", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 12:34:08", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 12:34:09", DF)));

        assertFalse(cf.matches(DateFormatUtils.fromString("2000-02-01 12:34:01", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-02 12:34:03", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-02-02 12:34:05", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-03-03 12:34:07", DF)));
    }

    public void testMultiValuesMinute() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* 1,3,5,7 * 1 1 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 12:00:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-01 12:01:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 12:02:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-01 12:03:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 12:04:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-01 12:05:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 12:06:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-01 12:07:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 12:08:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 12:09:34", DF)));

        assertFalse(cf.matches(DateFormatUtils.fromString("2000-02-01 12:01:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-02 12:03:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-02-02 12:05:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-03-03 12:07:34", DF)));
    }

    public void testMultiValuesHour() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * 1,3,5,7 1 1 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 00:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-01 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 02:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-01 03:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 04:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-01 05:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 06:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-01 07:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 08:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 09:12:34", DF)));

        assertFalse(cf.matches(DateFormatUtils.fromString("2000-02-01 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-02 03:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-02-02 05:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-03-03 07:12:34", DF)));
    }

    public void testMultiValuesDay() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * 1 1,3,5,7 1 *");
        assertNotNull(cf);
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-01 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-02 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-03 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-04 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-05 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-06 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-07 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-08 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-09 01:12:34", DF)));

        assertFalse(cf.matches(DateFormatUtils.fromString("2000-02-01 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-03 02:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-02-05 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-03-07 01:12:34", DF)));
    }

    public void testMultiValuesMonth() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * 1 * 1,3,5,7 *");
        assertNotNull(cf);
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-01 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-02-01 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-03-01 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-04-01 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-05-01 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-06-01 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-07-01 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-08-01 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-09-01 01:12:34", DF)));

        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 02:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-03-01 04:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-05-01 06:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-07-01 08:12:34", DF)));
    }

    public void testMultiValuesMonth2() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * 1 * Jan,MARCH,may,jUL *");
        assertNotNull(cf);
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-01-01 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-02-01 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-03-01 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-04-01 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-05-01 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-06-01 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2000-07-01 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-08-01 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-09-01 01:12:34", DF)));

        assertFalse(cf.matches(DateFormatUtils.fromString("2000-01-01 02:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-03-01 04:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-05-01 06:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2000-07-01 08:12:34", DF)));
    }

    public void testMultiValuesDow() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* * * * * 1,3,5,7");
        assertNotNull(cf);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertTrue(cf.matches(cal)); // 1
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 2
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 3
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 4
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 5
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 6
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 7
    }

    public void testMultiValuesDow2() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* * * * * Sun,TUE,thursday,sAt");
        assertNotNull(cf);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertTrue(cf.matches(cal)); // 1
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 2
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 3
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 4
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 5
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 6
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 7
    }

    public void testRangeSecond() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("15-20 * * 1 3 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-03-01 12:34:14", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 12:34:15", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 12:34:16", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 12:34:17", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 12:34:18", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 12:34:19", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 12:34:20", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-03-01 12:34:21", DF)));

        assertFalse(cf.matches(DateFormatUtils.fromString("1981-03-02 12:34:15", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-01 12:34:16", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-01 12:34:17", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-05-02 12:34:18", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-06-03 12:34:19", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-07-04 12:34:20", DF)));
    }

    public void testRangeMinute() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* 15-20 * 1 3 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-03-01 12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 12:15:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 12:16:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 12:17:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 12:18:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 12:19:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 12:20:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-03-01 12:21:34", DF)));

        assertFalse(cf.matches(DateFormatUtils.fromString("1981-03-02 12:15:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-01 12:16:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-01 12:17:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-05-02 12:18:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-06-03 12:19:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-07-04 12:20:34", DF)));
    }

    public void testRangeHour() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * 15-20 1 3 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-03-01 14:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 15:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 16:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 17:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 18:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 19:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-01 20:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-03-01 21:12:34", DF)));

        assertFalse(cf.matches(DateFormatUtils.fromString("1981-03-02 15:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-01 16:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-01 17:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-05-02 18:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-06-03 19:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-07-04 20:12:34", DF)));
    }

    public void testRangeDay() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * 1 15-20 3 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-03-14 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-15 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-16 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-17 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-18 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-19 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-20 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-03-21 01:12:34", DF)));

        assertFalse(cf.matches(DateFormatUtils.fromString("1981-03-15 02:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-16 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-17 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-05-18 02:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-06-19 03:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-07-20 04:12:34", DF)));
    }

    public void testRangeMonth() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * 1 3 5-7 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-03 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-05-03 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-06-03 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-07-03 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-08-03 01:12:34", DF)));

        assertFalse(cf.matches(DateFormatUtils.fromString("1981-05-01 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-06-01 03:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-07-03 03:12:34", DF)));
    }

    public void testRangeMonth2() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * 1 3 mAy-JULY *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-03 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-05-03 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-06-03 01:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-07-03 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-08-03 01:12:34", DF)));

        assertFalse(cf.matches(DateFormatUtils.fromString("1981-05-01 01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-06-01 03:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-07-03 03:12:34", DF)));
    }

    public void testRangeDow() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* * * * * 4-6");
        assertNotNull(cf);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertFalse(cf.matches(cal)); // 1
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 2
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 3
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 4
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 5
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 6
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 7
    }

    public void testRangeDow2() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* * * * * wEd-friDAY");
        assertNotNull(cf);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertFalse(cf.matches(cal)); // 1
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 2
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 3
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 4
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 5
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 6
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 7
    }

    public void testMultiRangesSecond() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("15-20,25-30 * * 29 4 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:14", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:15", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:16", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:17", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:18", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:19", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:20", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:21", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:22", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:23", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:24", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:25", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:26", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:27", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:28", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:29", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:30", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 12:34:31", DF)));
    }

    public void testMultiRangesMinute() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* 15-20,25-30 * 29 4 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:15:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:16:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:17:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:18:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:19:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:20:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 12:21:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 12:22:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 12:23:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 12:24:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:25:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:26:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:27:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:28:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:29:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 12:30:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 12:31:34", DF)));
    }

    public void testMultiRangesHour() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * 5-10,15-20 29 4 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 04:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 05:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 06:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 07:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 08:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 09:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 10:14:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 11:14:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 12:14:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 13:14:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 14:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 15:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 16:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 17:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 18:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 19:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-29 20:14:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-04-29 21:14:34", DF)));
    }

    public void testMultiRangesDay() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * * 15-20,25-30 5 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-05-14 12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-05-15 12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-05-16 12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-05-17 12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-05-18 12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-05-19 12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-05-20 12:14:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-05-21 12:14:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-05-22 12:14:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-05-23 12:14:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-05-24 12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-05-25 12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-05-26 12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-05-27 12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-05-28 12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-05-29 12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-05-30 12:14:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-05-31 12:14:34", DF)));
    }

    public void testMultiRangesMonth() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * * * 3-5,7-10 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-02-03 03:04:05", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-04 04:05:06", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-05 05:06:07", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-05-06 06:07:08", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-06-07 07:08:09", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-07-08 08:09:10", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-08-09 09:10:11", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-09-10 10:11:12", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-10-11 11:12:13", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-11-12 12:13:14", DF)));
    }

    public void testMultiRangesMonth2() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * * * march-MAY,jUl-OcToBer *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-02-03 03:04:05", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-03-04 04:05:06", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-04-05 05:06:07", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-05-06 06:07:08", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-06-07 07:08:09", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-07-08 08:09:10", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-08-09 09:10:11", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-09-10 10:11:12", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("1981-10-11 11:12:13", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("1981-11-12 12:13:14", DF)));
    }

    public void testMultiRangesDow() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* * * * * 2-3,5-6");
        assertNotNull(cf);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertFalse(cf.matches(cal)); // 1
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 2
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 3
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 4
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 5
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 6
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 7
    }

    public void testMultiRangesDow2() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* * * * * monday-TUE,ThursDAY-fRi");
        assertNotNull(cf);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertFalse(cf.matches(cal)); // 1
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 2
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 3
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 4
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 5
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 6
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 7
    }

    public void testMixedValuesAndRangesSecond() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("15-20,22,23,25-30 * * 17 02 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:14", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:15", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:16", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:17", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:18", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:19", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:20", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:21", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:22", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:23", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:24", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:25", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:26", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:27", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:28", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:29", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:30", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-02-17 12:34:31", DF)));
    }

    public void testMixedValuesAndRangesMinute() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* 15-20,22,23,25-30 * 17 02 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-02-17 12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:15:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:16:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:17:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:18:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:19:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:20:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-02-17 12:21:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:22:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:23:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-02-17 12:24:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:25:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:26:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:27:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:28:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:29:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:30:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-02-17 12:31:34", DF)));
    }

    public void testMixedValuesAndRangesHour() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * 1-5,7,9,11-15 17 02 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-02-17 00:14:30", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 01:14:31", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 02:14:32", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 03:14:33", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 04:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 05:14:35", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-02-17 06:14:36", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 07:14:37", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-02-17 08:14:38", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 09:14:39", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-02-17 10:14:40", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 11:14:41", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 12:14:52", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 13:14:23", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 14:14:45", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-17 15:14:33", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-02-17 16:14:56", DF)));
    }

    public void testMixedValuesAndRangesDay() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * * 15-20,22,23,25-30 03 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-03-14 01:02:03", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-03-15 02:03:04", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-03-16 03:04:05", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-03-17 04:05:06", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-03-18 05:06:07", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-03-19 06:07:08", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-03-20 07:08:09", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-03-21 08:09:10", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-03-22 09:10:11", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-03-23 10:11:12", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-03-24 11:12:13", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-03-25 12:13:14", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-03-26 13:14:15", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-03-27 14:15:16", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-03-28 15:16:17", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-03-29 16:17:18", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-03-30 17:18:19", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-03-31 18:19:20", DF)));
    }

    public void testMixedValuesAndRangesMonth() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * * * 2-4,6,7,9-11 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-01-02 00:14:30", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-03 01:14:31", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-03-04 02:14:32", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-04-05 03:14:33", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-05-06 04:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-06-07 05:14:35", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-07-08 06:14:36", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-08-09 07:14:37", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-09-10 08:14:38", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-10-11 09:14:39", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-11-12 10:14:40", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-12-13 11:14:41", DF)));
    }

    public void testMixedValuesAndRangesMonth2() throws InterruptedException {
        final String DF = "yyyy-MM-dd HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * * * feb-APRIL,jUn,JuLy,Sep-nOV *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-01-02 00:14:30", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-02-03 01:14:31", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-03-04 02:14:32", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-04-05 03:14:33", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-05-06 04:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-06-07 05:14:35", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-07-08 06:14:36", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-08-09 07:14:37", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-09-10 08:14:38", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-10-11 09:14:39", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("2018-11-12 10:14:40", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("2018-12-13 11:14:41", DF)));
    }

    public void testMixedValuesAndRangesDow() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* * * * * 2-3,5,6-7");
        assertNotNull(cf);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertFalse(cf.matches(cal)); // 1
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 2
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 3
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 4
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 5
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 6
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 7
    }

    public void testMixedValuesAndRangesDow2() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* * * * * mon-TUESDAY,tHu,FridaY-sATURDAy");
        assertNotNull(cf);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertFalse(cf.matches(cal)); // 1
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 2
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 3
        assertFalse(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 4
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 5
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 6
        assertTrue(cf.matches(cal = DateTimeUtils.nextDay(cal))); // 7
    }

    public void testDayOfWeekNumber() throws InterruptedException {
        final int[] DOWLIST = { Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
                Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY };
        CronFormat cf = CronFormat
                .parse("* * * * * " + CronFormat.MONDAY + "-" + CronFormat.WEDNESDAY);
        assertNotNull(cf);
        int count = 0;
        Calendar cal = Calendar.getInstance();
        for (int dow : DOWLIST) {
            cal.set(Calendar.DAY_OF_WEEK, dow);
            count += cf.matches(cal) ? 1 : 0;
        }
        assertEquals(3, count);
    }

    public void testDayOfWeekNameShort() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* * * * * Wed,MON,fri");
        assertNotNull(cf);
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        assertTrue(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        assertFalse(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        assertTrue(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        assertFalse(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        assertTrue(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        assertFalse(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertFalse(cf.matches(cal));
    }

    public void testDayOfWeekNameShort2() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* * * * * sun-WED");
        assertNotNull(cf);
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        assertTrue(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        assertTrue(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        assertTrue(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        assertFalse(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        assertFalse(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        assertFalse(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertTrue(cf.matches(cal));
    }

    public void testDayOfWeekNameLong() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* * * * * Wednesday,MONDAY,friday");
        assertNotNull(cf);
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        assertTrue(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        assertFalse(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        assertTrue(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        assertFalse(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        assertTrue(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        assertFalse(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertFalse(cf.matches(cal));
    }

    public void testDayOfWeekNNameLong2() throws InterruptedException {
        CronFormat cf = CronFormat.parse("* * * * * sunday-WEDNESDAY");
        assertNotNull(cf);
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        assertTrue(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        assertTrue(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        assertTrue(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        assertFalse(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        assertFalse(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        assertFalse(cf.matches(cal));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertTrue(cf.matches(cal));
    }
}
