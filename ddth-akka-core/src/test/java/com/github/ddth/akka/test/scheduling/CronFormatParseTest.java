package com.github.ddth.akka.test.scheduling;

import com.github.ddth.akka.scheduling.CronFormat;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CronFormatParseTest extends TestCase {

    public CronFormatParseTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(CronFormatParseTest.class);
    }

    public static Throwable runException(Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            return t;
        }
        return null;
    }

    public void testParseShortSecond() {
        assertTrue(
                runException(() -> CronFormat.parse("-1 * *")) instanceof IllegalArgumentException);
        assertTrue(
                runException(() -> CronFormat.parse("60 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("1-60 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("1,60 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("1,2-60 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("1,2-59,60 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("-1-6 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("-1,6 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("-1,2-6 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("-1,2-5,6 * *")) instanceof IllegalArgumentException);
    }

    public void testParseShortMinute() {
        assertTrue(
                runException(() -> CronFormat.parse("* -1 *")) instanceof IllegalArgumentException);
        assertTrue(
                runException(() -> CronFormat.parse("* 60 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* 1-60 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* 1,60 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* 1,2-60 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* 1,2-59,60 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* -1-6 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* -1,6 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* -1,2-6 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* -1,2-5,6 *")) instanceof IllegalArgumentException);
    }

    public void testParseShortHour() {
        assertTrue(
                runException(() -> CronFormat.parse("* * -1")) instanceof IllegalArgumentException);
        assertTrue(
                runException(() -> CronFormat.parse("* * 24")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * 1-24")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * 1,24")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * 1,2-24")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * 1,2-23,24")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * -1-6")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * -1,6")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * -1,2-6")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * -1,2-5,6")) instanceof IllegalArgumentException);
    }

    public void testParse1() {
        assertTrue(runException(() -> CronFormat.parse("*")) instanceof IllegalArgumentException);
        assertTrue(runException(() -> CronFormat.parse(" *")) instanceof IllegalArgumentException);
        assertTrue(runException(() -> CronFormat.parse("* ")) instanceof IllegalArgumentException);
        assertTrue(runException(() -> CronFormat.parse(" * ")) instanceof IllegalArgumentException);
        assertTrue(runException(() -> CronFormat.parse("**")) instanceof IllegalArgumentException);
        assertTrue(runException(() -> CronFormat.parse(" **")) instanceof IllegalArgumentException);
        assertTrue(runException(() -> CronFormat.parse("** ")) instanceof IllegalArgumentException);
        assertTrue(
                runException(() -> CronFormat.parse(" ** ")) instanceof IllegalArgumentException);
    }

    public void testParse2() {
        assertTrue(runException(() -> CronFormat.parse("* *")) instanceof IllegalArgumentException);
        assertTrue(
                runException(() -> CronFormat.parse(" * *")) instanceof IllegalArgumentException);
        assertTrue(
                runException(() -> CronFormat.parse("* * ")) instanceof IllegalArgumentException);
        assertTrue(
                runException(() -> CronFormat.parse(" * * ")) instanceof IllegalArgumentException);
        assertTrue(
                runException(() -> CronFormat.parse("*  *")) instanceof IllegalArgumentException);
        assertTrue(
                runException(() -> CronFormat.parse("  *  *")) instanceof IllegalArgumentException);
        assertTrue(
                runException(() -> CronFormat.parse("*  *  ")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("  *  *  ")) instanceof IllegalArgumentException);
    }

    public void testParse4() {
        assertTrue(runException(
                () -> CronFormat.parse("* * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse(" * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * ")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse(" * * * * ")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("*  *     *   *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("  *  *    *    *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("*    *    *  *  ")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("  *  *   *     *  ")) instanceof IllegalArgumentException);
    }

    public void testParse5() {
        assertTrue(runException(
                () -> CronFormat.parse("* * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse(" * * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * * ")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse(" * * * * * ")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("*  *     *   *  *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("  *  *    *    * *")) instanceof IllegalArgumentException);
        assertTrue(runException(() -> CronFormat
                .parse("*   *    *    *  *  ")) instanceof IllegalArgumentException);
        assertTrue(runException(() -> CronFormat
                .parse("  *  *   *     * *  ")) instanceof IllegalArgumentException);
    }

    public void testParse7() {
        assertTrue(runException(
                () -> CronFormat.parse("* * * * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse(" * * * * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * * * * ")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse(" * * * * * * * ")) instanceof IllegalArgumentException);
        assertTrue(runException(() -> CronFormat
                .parse("*  *     *   *  * *   *")) instanceof IllegalArgumentException);
        assertTrue(runException(() -> CronFormat
                .parse("  *  *    *    * *   * *")) instanceof IllegalArgumentException);
        assertTrue(runException(() -> CronFormat
                .parse("*   *  *   *    *    *  *  ")) instanceof IllegalArgumentException);
        assertTrue(runException(() -> CronFormat
                .parse("  *  *   *     * *  * *")) instanceof IllegalArgumentException);
    }

    public void testParseLongSecond() {
        assertTrue(runException(
                () -> CronFormat.parse("-1 * * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("60 * * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("1-60 * * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("1,60 * * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("1,2-60 * * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("1,2-59,60 * * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("-1-6 * * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("-1,6 * * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("-1,2-6 * * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("-1,2-5,6 * * * * *")) instanceof IllegalArgumentException);
    }

    public void testParseLongMinute() {
        assertTrue(runException(
                () -> CronFormat.parse("* -1 * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* 60 * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* 1-60 * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* 1,60 * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* 1,2-60 * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* 1,2-59,60 * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* -1-6 * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* -1,6 * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* -1,2-6 * * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* -1,2-5,6 * * * *")) instanceof IllegalArgumentException);
    }

    public void testParseLongHour() {
        assertTrue(runException(
                () -> CronFormat.parse("* * -1 * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * 24 * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * 1-24 * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * 1,24 * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * 1,2-24 * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * 1,2-23,24 * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * -1-6 * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * -1,6 * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * -1,2-6 * * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * -1,2-5,6 * * *")) instanceof IllegalArgumentException);
    }

    public void testParseLongDay() {
        assertTrue(runException(
                () -> CronFormat.parse("* * * -1 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * 32 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * 1-32 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * 1,32 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * 1,2-32 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * 1,2-31,32 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * -1-6 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * -1,6 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * -1,2-6 * *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * -1,2-5,6 * *")) instanceof IllegalArgumentException);
    }

    public void testParseLongMonth() {
        assertTrue(runException(
                () -> CronFormat.parse("* * * * -1 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * 13 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * 1-13 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * 1,13 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * 1,2-13 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * 1,2-12,13 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * -1-6 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * -1,6 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * -1,2-6 *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * -1,2-5,6 *")) instanceof IllegalArgumentException);

        assertTrue(runException(
                () -> CronFormat.parse("* * * * Janu *")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * June-Janu *")) instanceof IllegalArgumentException);
        assertTrue(runException(() -> CronFormat
                .parse("* * * * June,augu *")) instanceof IllegalArgumentException);
        assertTrue(runException(() -> CronFormat
                .parse("* * * * Jan,Feb-April,augu *")) instanceof IllegalArgumentException);
    }

    public void testParseLongDow() {
        assertTrue(runException(
                () -> CronFormat.parse("* * * * * -1")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * * 8")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * * 1-8")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * * 1,8")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * * 1,2-8")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * * 1,2-7,8")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * * -1-6")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * * -1,6")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * * -1,2-6")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * * -1,2-5,6")) instanceof IllegalArgumentException);

        assertTrue(runException(
                () -> CronFormat.parse("* * * * * Mond")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * * Mon-Wedn")) instanceof IllegalArgumentException);
        assertTrue(runException(
                () -> CronFormat.parse("* * * * * Mon,wedn")) instanceof IllegalArgumentException);
        assertTrue(runException(() -> CronFormat
                .parse("* * * * * Monday,Tue-Fri,satu")) instanceof IllegalArgumentException);
    }
}
