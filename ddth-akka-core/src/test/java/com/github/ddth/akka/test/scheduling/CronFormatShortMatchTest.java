package com.github.ddth.akka.test.scheduling;

import java.util.Calendar;
import java.util.Date;

import com.github.ddth.akka.scheduling.CronFormat;
import com.github.ddth.commons.utils.DateFormatUtils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CronFormatShortMatchTest extends TestCase {

    public CronFormatShortMatchTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(CronFormatShortMatchTest.class);
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
        CronFormat cf = CronFormat.parse("* * *");
        assertNotNull(cf);
        assertTrue(cf.matches(System.currentTimeMillis()));
        assertTrue(cf.matches(new Date()));
        assertTrue(cf.matches(Calendar.getInstance()));
    }

    public void testEvery3Seconds() throws InterruptedException {
        CronFormat cf = CronFormat.parse("*/3 * *");
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
        CronFormat cf = CronFormat.parse("* */3 *");
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
        CronFormat cf = CronFormat.parse("* * */3");
        assertNotNull(cf);
        Calendar cal = Calendar.getInstance();
        int matches = 0;
        for (int i = 0; i < 3 * 2; i++) {
            cal.set(Calendar.HOUR, 1 + i);
            matches += cf.matches(cal) ? 1 : 0;
        }
        assertEquals(2, matches);
    }

    public void testMultiValuesSecond() throws InterruptedException {
        final String DF = "HH:mm:ss";
        CronFormat cf = CronFormat.parse("1,3,5,7 * *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:00", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:01", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:02", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:03", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:04", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:05", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:06", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:07", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:08", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:09", DF)));
    }

    public void testMultiValuesMinute() throws InterruptedException {
        final String DF = "HH:mm:ss";
        CronFormat cf = CronFormat.parse("* 1,3,5,7 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("12:00:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:01:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:02:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:03:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:04:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:05:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:06:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:07:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:08:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:09:34", DF)));
    }

    public void testMultiValuesHour() throws InterruptedException {
        final String DF = "HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * 1,3,5,7");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("00:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("01:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("02:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("03:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("04:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("05:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("06:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("07:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("08:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("09:12:34", DF)));
    }

    public void testRangeSecond() throws InterruptedException {
        final String DF = "HH:mm:ss";
        CronFormat cf = CronFormat.parse("15-20 * *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:14", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:15", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:16", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:17", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:18", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:19", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:20", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:21", DF)));
    }

    public void testRangeMinute() throws InterruptedException {
        final String DF = "HH:mm:ss";
        CronFormat cf = CronFormat.parse("* 15-20 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:15:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:16:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:17:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:18:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:19:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:20:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:21:34", DF)));
    }

    public void testRangeHour() throws InterruptedException {
        final String DF = "HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * 15-20");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("14:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("15:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("16:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("17:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("18:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("19:12:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("20:12:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("21:12:34", DF)));
    }

    public void testMultiRangesSecond() throws InterruptedException {
        final String DF = "HH:mm:ss";
        CronFormat cf = CronFormat.parse("15-20,25-30 * *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:14", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:15", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:16", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:17", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:18", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:19", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:20", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:21", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:22", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:23", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:24", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:25", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:26", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:27", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:28", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:29", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:30", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:31", DF)));
    }

    public void testMultiRangesMinute() throws InterruptedException {
        final String DF = "HH:mm:ss";
        CronFormat cf = CronFormat.parse("* 15-20,25-30 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:15:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:16:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:17:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:18:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:19:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:20:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:21:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:22:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:23:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:24:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:25:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:26:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:27:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:28:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:29:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:30:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:31:34", DF)));
    }

    public void testMultiRangesHour() throws InterruptedException {
        final String DF = "HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * 1-5,10-12");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("00:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("01:15:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("02:16:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("03:17:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("04:18:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("05:19:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("06:20:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("07:21:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("08:22:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("09:23:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("10:24:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("11:25:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:26:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("13:27:34", DF)));
    }

    public void testMixedValuesAndRangesSecond() throws InterruptedException {
        final String DF = "HH:mm:ss";
        CronFormat cf = CronFormat.parse("15-20,22,23,25-30 * *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:14", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:15", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:16", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:17", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:18", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:19", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:20", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:21", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:22", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:23", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:24", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:25", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:26", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:27", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:28", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:29", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:34:30", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:34:31", DF)));
    }

    public void testMixedValuesAndRangesMinute() throws InterruptedException {
        final String DF = "HH:mm:ss";
        CronFormat cf = CronFormat.parse("* 15-20,22,23,25-30 *");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("12:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:15:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:16:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:17:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:18:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:19:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:20:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:21:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:22:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:23:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:24:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:25:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:26:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:27:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:28:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:29:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("12:30:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:31:34", DF)));
    }

    public void testMixedValuesAndRangesHour() throws InterruptedException {
        final String DF = "HH:mm:ss";
        CronFormat cf = CronFormat.parse("* * 1-3,5,7,9-11");
        assertNotNull(cf);
        assertFalse(cf.matches(DateFormatUtils.fromString("00:14:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("01:15:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("02:16:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("03:17:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("04:18:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("05:19:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("06:20:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("07:21:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("08:22:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("09:23:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("10:24:34", DF)));
        assertTrue(cf.matches(DateFormatUtils.fromString("11:25:34", DF)));
        assertFalse(cf.matches(DateFormatUtils.fromString("12:26:34", DF)));
    }
}
