package com.github.ddth.akka.scheduling;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Cron-like format for worker to match against a "tick".
 *
 * <p>
 * Format: {@code <Second (0-59)> <Minute (0-59)>
 * <Hour (0-23)> <DoM (1-31)> <MoY (1-12)> <DoW (1:Sunday-7:Saturday)>}
 * </p>
 * <p>
 * For {@code Month_of_Year} and {@code Day_of_Week} fields, full names (
 * {@code "January,February,...,December"} or
 * {@code "Sunday,Monday,Tuesday...,Saturday"}) or abbreviations (
 * {@code "Jan,Feb,...,Dec"} or {@code "Sun,Mon,Tue,...,Sat"}) can be used
 * instead of numeric values.
 * </p>
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class CronFormat {

    private static class SchedulePart {
        private final static Pattern PATTERN_TICK = Pattern.compile("^\\*\\/(\\d+)$");
        private final static Pattern PATTERN_RANGE = Pattern.compile("^(\\d+)-(\\d+)$");
        private final static Pattern PATTERN_RANGE_NAME = Pattern.compile("^([A-Z]+)-([A-Z]+)$",
                Pattern.CASE_INSENSITIVE);
        private final static Pattern PATTERN_EXACT = Pattern.compile("^(\\d+)$");
        private final static Pattern PATTERN_EXACT_NAME = Pattern.compile("^([A-Z]+)$",
                Pattern.CASE_INSENSITIVE);

        /**
         * Used to test second, minute, and hour parts.
         *
         * @param schedule
         * @param min
         * @param max
         */
        protected static boolean isValidSchedule(String schedule, int min, int max) {
            if (StringUtils.isBlank(schedule)) {
                return false;
            }
            if (StringUtils.equals("*", schedule)) {
                return true;
            }
            Matcher mTick = PATTERN_TICK.matcher(schedule);
            if (mTick.matches()) {
                int tick = Integer.parseInt(mTick.group(1));
                return 0 < tick && min <= tick && tick <= max;
            }
            String[] tokens = schedule.split(",");
            for (String token : tokens) {
                Matcher mExact = PATTERN_EXACT.matcher(token);
                if (mExact.matches()) {
                    int value = Integer.parseInt(mExact.group(1));
                    if (value < min || value > max) {
                        return false;
                    }
                    continue;
                }

                Matcher mRange = PATTERN_RANGE.matcher(token);
                if (mRange.matches()) {
                    int vLow = Integer.parseInt(mRange.group(1));
                    int vHigh = Integer.parseInt(mRange.group(2));
                    if (vLow > vHigh || vLow < min || vHigh > max) {
                        return false;
                    }
                    continue;
                }
                return false;
            }
            return true;
        }

        /**
         * Used to test month and day-of-week parts.
         *
         * @param schedule
         * @param acceptedValues
         * @return
         */
        protected static boolean isValidSchedule(String schedule, String[] acceptedValues) {
            if (StringUtils.isBlank(schedule)) {
                return false;
            }
            if (StringUtils.equals("*", schedule)) {
                return true;
            }
            String[] tokens = schedule.split(",");
            for (String token : tokens) {
                Matcher mExact = PATTERN_EXACT_NAME.matcher(token);
                if (mExact.matches()) {
                    boolean matches = false;
                    String v = mExact.group(1);
                    for (String acceptedValue : acceptedValues) {
                        if (StringUtils.equalsAnyIgnoreCase(v, acceptedValues) || (v.length() == 3
                                && StringUtils.startsWithIgnoreCase(acceptedValue, v))) {
                            matches = true;
                            break;
                        }
                    }
                    if (matches) {
                        continue;
                    } else {
                        return false;
                    }
                }

                Matcher mRange = PATTERN_RANGE_NAME.matcher(token);
                if (mRange.matches()) {
                    String vLow = mRange.group(1);
                    String vHigh = mRange.group(2);
                    boolean matchLow = false, matchHigh = false;
                    for (String acceptedValue : acceptedValues) {
                        matchLow |= StringUtils.equalsAnyIgnoreCase(vLow, acceptedValues)
                                || (vLow.length() == 3
                                        && StringUtils.startsWithIgnoreCase(acceptedValue, vLow));
                        matchHigh |= StringUtils.equalsAnyIgnoreCase(vHigh, acceptedValues)
                                || (vHigh.length() == 3
                                        && StringUtils.startsWithIgnoreCase(acceptedValue, vHigh));
                        if (matchLow && matchHigh) {
                            break;
                        }
                    }
                    if (matchLow && matchHigh) {
                        continue;
                    } else {
                        return false;
                    }
                }
                return false;
            }
            return true;
        }

        protected static boolean matches(String[] tokens, int value) {
            for (String token : tokens) {
                if (StringUtils.equals(token, "*")) {
                    return true;
                }

                Matcher mTick = PATTERN_TICK.matcher(token);
                if (mTick.matches()) {
                    int div = Integer.parseInt(mTick.group(1));
                    if ((div > 0) && (value % div == 0)) {
                        return true;
                    }
                    continue;
                }

                Matcher mExact = PATTERN_EXACT.matcher(token);
                if (mExact.matches()) {
                    if (value == Integer.parseInt(token)) {
                        return true;
                    }
                    continue;
                }

                Matcher mRange = PATTERN_RANGE.matcher(token);
                if (mRange.matches()) {
                    int min = Integer.parseInt(mRange.group(1));
                    int max = Integer.parseInt(mRange.group(2));
                    if (min <= value && value <= max) {
                        return true;
                    }
                    continue;
                }
            }
            return false;
        }

        protected static boolean matches(String[] tokens, String value, String[] values) {
            int index = ArrayUtils.indexOf(values, value);
            // for (int i = 0; i < values.length; i++) {
            // if (StringUtils.equalsIgnoreCase(value, values[i])) {
            // index = i;
            // break;
            // }
            // }
            for (String token : tokens) {
                if (StringUtils.equals(token, "*")) {
                    return true;
                }

                Matcher mExact = PATTERN_EXACT_NAME.matcher(token);
                if (mExact.matches()) {
                    String v = mExact.group(1);
                    if (StringUtils.equalsAnyIgnoreCase(v, value)
                            || (v.length() == 3 && StringUtils.startsWithIgnoreCase(value, v))) {
                        return true;
                    }
                    continue;
                }

                Matcher mRange = PATTERN_RANGE_NAME.matcher(token);
                if (mRange.matches()) {
                    String vLow = mRange.group(1);
                    String vHigh = mRange.group(2);
                    int min = -1, max = -1;
                    for (int i = 0; i < values.length; i++) {
                        if (StringUtils.equalsAnyIgnoreCase(vLow, values[i]) || (vLow.length() == 3
                                && StringUtils.startsWithIgnoreCase(values[i], vLow))) {
                            min = i;
                        }
                        if (StringUtils.equalsAnyIgnoreCase(vHigh, values[i])
                                || (vHigh.length() == 3
                                        && StringUtils.startsWithIgnoreCase(values[i], vHigh))) {
                            max = i;
                        }
                    }
                    if (min >= 0 && max >= 0 && min <= index && index <= max) {
                        return true;
                    }
                    continue;
                }
            }
            return false;
        }

        /**
         * Create a new schedule part with default schedule value.
         * 
         * @param clazz
         * @return
         */
        public static <T extends SchedulePart> T newInstance(Class<T> clazz) {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                throw e instanceof RuntimeException ? (RuntimeException) e
                        : new RuntimeException(e);
            }
        }

        /**
         * Create a new schedule part with specified schedule value.
         * 
         * @param schedule
         * @param clazz
         * @return
         */
        public static <T extends SchedulePart> T newInstance(String schedule, Class<T> clazz) {
            T part = newInstance(clazz);
            part.setSchedule(schedule);
            return part;
        }

        private SchedulePart() {
        }

        protected String schedule = "*";
        protected String[] tokens = { "*" };

        protected String getSchedule() {
            return schedule;
        }

        protected SchedulePart setSchedule(String schedule) {
            if (!isValidSchedule(schedule)) {
                throw new IllegalArgumentException("Invalid pattern [" + schedule + "]");
            }
            this.schedule = schedule.trim();
            this.tokens = this.schedule.split("[,;]+");
            return this;
        }

        protected boolean isValidValue(int value) {
            return true;
        }

        protected boolean isValidSchedule(String schedule) {
            return true;
        }

        /**
         * Schedule to run at Nth cycle.
         * 
         * @param n
         * @return
         */
        public SchedulePart at(int n) {
            if (!isValidValue(n)) {
                throw new IllegalArgumentException("Invalid value [" + n + "]!");
            }
            this.schedule = String.valueOf(n);
            return this;
        }

        /**
         * Schedule to run every N cycles.
         */
        public SchedulePart every(int n) {
            if (!isValidValue(n)) {
                throw new IllegalArgumentException("Invalid value [" + n + "]!");
            }
            this.schedule = n <= 1 ? "*" : ("*/" + n);
            return this;
        }

        public boolean matches(int value) {
            return isValidValue(value) && matches(tokens, value);
        }
    }

    public static class Second extends SchedulePart {
        private final static int MIN = 0, MAX = 59;

        @Override
        protected boolean isValidValue(int value) {
            return MIN <= value && value <= MAX;
        }

        @Override
        protected boolean isValidSchedule(String schedule) {
            return isValidSchedule(schedule, MIN, MAX);
        }
    }

    public static class Minute extends SchedulePart {
        private final static int MIN = 0, MAX = 59;

        @Override
        protected boolean isValidValue(int value) {
            return MIN <= value && value <= MAX;
        }

        @Override
        protected boolean isValidSchedule(String schedule) {
            return isValidSchedule(schedule, MIN, MAX);
        }
    }

    public static class Hour extends SchedulePart {
        private final static int MIN = 0, MAX = 23;

        @Override
        protected boolean isValidValue(int value) {
            return MIN <= value && value <= MAX;
        }

        @Override
        protected boolean isValidSchedule(String schedule) {
            return isValidSchedule(schedule, MIN, MAX);
        }
    }

    public static class DayOfMonth extends SchedulePart {
        private final static int MIN = 1, MAX = 31;

        @Override
        protected boolean isValidValue(int value) {
            return MIN <= value && value <= MAX;
        }

        @Override
        protected boolean isValidSchedule(String schedule) {
            return isValidSchedule(schedule, MIN, MAX);
        }
    }

    public static class Month extends SchedulePart {
        private final static int MIN = JANUARY, MAX = DECEMBER;

        @Override
        protected boolean isValidValue(int value) {
            return MIN <= value && value <= MAX;
        }

        @Override
        protected boolean isValidSchedule(String schedule) {
            return isValidSchedule(schedule, MIN, MAX) || isValidSchedule(schedule, MONTH_LIST);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(int value) {
            return isValidValue(value) && (matches(tokens, value)
                    || matches(tokens, MONTH_LIST[value - 1], MONTH_LIST));
        }
    }

    public static class DayOfWeek extends SchedulePart {
        private final static int MIN = SUNDAY, MAX = SATURDAY;

        @Override
        protected boolean isValidValue(int value) {
            return MIN <= value && value <= MAX;
        }

        @Override
        protected boolean isValidSchedule(String schedule) {
            return isValidSchedule(schedule, MIN, MAX) || isValidSchedule(schedule, DOW_LIST);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(int value) {
            return isValidValue(value)
                    && (matches(tokens, value) || matches(tokens, DOW_LIST[value - 1], DOW_LIST));
        }
    }

    /**
     * Parses a cron format from a plain text string.
     * 
     * @param input
     *            either short format {@code <Second> <Minute> <Hour>} or full
     *            format
     *            {@code <Second> <Minute> <Hour> <Day_Of_Month> <Month> <Day_Of_Week>}
     * @return
     */
    public static CronFormat parse(String input) {
        String[] tokens = input.trim().split("[\\s\\t]+");
        if (tokens == null || (tokens.length != 3 && tokens.length != 6)) {
            throw new IllegalArgumentException("Invalid input [" + input + "]!");
        }
        if (tokens.length == 3) {
            return new CronFormat(tokens[0], tokens[1], tokens[2]);
        }
        if (tokens.length == 6) {
            return new CronFormat(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5]);
        }
        return null;
    }

    public final static int SUNDAY = 1, MONDAY = 2, TUESDAY = 3, WEDNESDAY = 4, THURSDAY = 5,
            FRIDAY = 6, SATURDAY = 7;
    public final static int JANUARY = 1, FEBRUARY = 2, MARCH = 3, APRIL = 4, MAY = 5, JUNE = 6,
            JULY = 7, AUGUST = 8, SEPTEMBER = 9, OCTOBER = 10, NOVEMBER = 11, DECEMBER = 12;
    private final static String[] DOW_LIST = { "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY",
            "THURSDAY", "FRIDAY", "SATURDAY" };
    private final static String[] MONTH_LIST = { "JANUARY", "FEBRUARY ", "MARCH", "APRIL", "MAY",
            "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER" };

    private Second second = SchedulePart.newInstance(Second.class);
    private Minute minute = SchedulePart.newInstance(Minute.class);
    private Hour hour = SchedulePart.newInstance(Hour.class);
    private DayOfMonth dayOfMonth = SchedulePart.newInstance(DayOfMonth.class);
    private Month month = SchedulePart.newInstance(Month.class);
    private DayOfWeek dayOfWeek = SchedulePart.newInstance(DayOfWeek.class);

    public CronFormat() {
    }

    public CronFormat(String second, String minute, String hour) {
        setSecond(second);
        setMinute(minute);
        setHour(hour);
    }

    public CronFormat(String second, String minute, String hour, String dayOfMonth, String month,
            String dayOfWeek) {
        setSecond(second);
        setMinute(minute);
        setHour(hour);
        setDayOfMonth(dayOfMonth);
        setMonth(month);
        setDayOfWeek(dayOfWeek);
    }

    /**
     * Matches this cron format against a timestamp.
     * 
     * @param timestampMillis
     * @return
     */
    public boolean matches(long timestampMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestampMillis);
        return matches(cal);
    }

    /**
     * Matches this cron format against a timestamp.
     * 
     * @param timestamp
     * @return
     */
    public boolean matches(Date timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        return matches(cal);
    }

    /**
     * Matches this cron format against a timestamp.
     * 
     * @param timestamp
     * @return
     */
    public boolean matches(Calendar timestamp) {
        if (!second.matches(timestamp.get(Calendar.SECOND))) {
            return false;
        }

        if (!minute.matches(timestamp.get(Calendar.MINUTE))) {
            return false;
        }

        if (!hour.matches(timestamp.get(Calendar.HOUR_OF_DAY))) {
            return false;
        }

        if (!dayOfMonth.matches(timestamp.get(Calendar.DAY_OF_MONTH))) {
            return false;
        }

        if (!month.matches(timestamp.get(Calendar.MONTH) + 1)) {
            return false;
        }

        int _valDow = timestamp.get(Calendar.DAY_OF_WEEK);
        int valDow = 0;
        switch (_valDow) {
        case Calendar.MONDAY:
            valDow = MONDAY;
            break;
        case Calendar.TUESDAY:
            valDow = TUESDAY;
            break;
        case Calendar.WEDNESDAY:
            valDow = WEDNESDAY;
            break;
        case Calendar.THURSDAY:
            valDow = THURSDAY;
            break;
        case Calendar.FRIDAY:
            valDow = FRIDAY;
            break;
        case Calendar.SATURDAY:
            valDow = SATURDAY;
            break;
        case Calendar.SUNDAY:
            valDow = SUNDAY;
            break;
        }
        if (!dayOfWeek.matches(valDow)) {
            return false;
        }

        return true;
    }

    /*----------------------------------------------------------------------*/
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return second + " " + minute + " " + hour + " " + dayOfMonth + " " + month + " "
                + dayOfWeek;
    }

    public CronFormat everyNMonths(int n) {
        if (n == 1) {
            setDayOfMonth("*");
        } else {
            setDayOfMonth("*/" + String.valueOf(n));
        }
        return this;
    }

    /**
     * Helper method to set "second" pattern.
     * 
     * @param second
     *            value in range {@code [0,59]}
     * @return
     */
    public CronFormat at(int second) {
        setSecond(String.valueOf(second));
        return this;
    }

    /**
     * Helper method to set "minute" and "second" parts.
     * 
     * @param minute
     *            value in range {@code [0,59]}
     * @param second
     *            value in range {@code [0,59]}
     * @return
     */
    public CronFormat at(int minute, int second) {
        setMinute(String.valueOf(minute));
        setSecond(String.valueOf(second));
        return this;
    }

    /**
     * Helper method to set "hour", "minute" and "second" parts.
     * 
     * @param hour
     *            value in range {@code [0,23]}
     * @param minute
     *            value in range {@code [0,59]}
     * @param second
     *            value in range {@code [0,59]}
     * @return
     */
    public CronFormat at(int hour, int minute, int second) {
        setHour(String.valueOf(hour));
        setMinute(String.valueOf(minute));
        setSecond(String.valueOf(second));
        return this;
    }

    public Second getSecond() {
        return second;
    }

    public CronFormat setSecond(String schedule) {
        second.setSchedule(schedule);
        return this;
    }

    public Minute getMinute() {
        return minute;
    }

    public CronFormat setMinute(String schedule) {
        minute.setSchedule(schedule);
        return this;
    }

    public Hour getHour() {
        return hour;
    }

    public CronFormat setHour(String schedule) {
        hour.setSchedule(schedule);
        return this;
    }

    public DayOfMonth getDayOfMonth() {
        return dayOfMonth;
    }

    public CronFormat setDayOfMonth(String schedule) {
        dayOfMonth.setSchedule(schedule);
        return this;
    }

    public Month getMonth() {
        return month;
    }

    public CronFormat setMonth(String schedule) {
        month.setSchedule(schedule);
        return this;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public CronFormat setDayOfWeek(String schedule) {
        dayOfWeek.setSchedule(schedule);
        return this;
    }
}
