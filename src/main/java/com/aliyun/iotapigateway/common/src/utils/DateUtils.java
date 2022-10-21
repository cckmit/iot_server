package com.aliyun.iotx.haas.tdserver.common.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtils {

    /**
     * milliseconds in a second.
     */
    public static final long   SECOND                = 1000;

    /**
     * milliseconds in a minute.
     */
    public static final long   MINUTE                = SECOND * 60;

    /**
     * milliseconds in a hour.
     */
    public static final long   HOUR                  = MINUTE * 60;

    /**
     * milliseconds in a day.
     */
    public static final long   DAY                   = 24 * HOUR;

    /**
     * yyyy-MM
     */
    public static final String MONTH_PATTERN         = "yyyy-MM";

    /**
     * yyyy-MM-dd
     */
    public static final String DEFAULT_PATTERN       = "yyyy-MM-dd";

    /**
     * yyyyMMdd
     */
    public static final String DEFAULT_PATTERN2       = "yyyyMMdd";

    /** *月*日 */
    public static final String MONTH_DAY_PATTERN       = "M月d日";

    /**
     * yyyyMMddHHmmss
     */
    public static final String FULL_PATTERN          = "yyyyMMddHHmmss";

    /**
     * yyyyMMddHHmmss
     */
    public static final String FULL_PATTERN2          = "YYYYMMDDHHMMSS";


    /**
     * yyyyMMdd HH:mm:ss
     */
    public static final String FULL_STANDARD_PATTERN = "yyyyMMdd HH:mm:ss";

    /**
     * MM.dd HH:mm
     */
    public static final String FULL_MATCH_PATTERN    = "MM.dd HH:mm";

    /**
     * HH:mm
     */
    public static final String SHORT_MATCH_PATTERN   = "HH:mm";

    /**
     * HH:mm:ss
     */
    public static final String SHORT_SECOND_MATCH_PATTERN   = "HH:mm:ss";

    /**
     * yyyy-MM-dd HH:mm
     */
    public static final String DATE_TIME_MINUTE      = "yyyy-MM-dd HH:mm";

    /**
     * <pre>
     * yyyy-MM-dd HH:mm:ss
     * </pre>
     */
    public static final String DATE_TIME_SHORT       = "yyyy-MM-dd HH:mm:ss";

    /**
     * <pre>
     * yyyy-MM-dd HH:mm:ss.SSS
     * </pre>
     */
    public static final String DATE_TIME_FULL        = "yyyy-MM-dd HH:mm:ss.SSS";

    public static final String DATE_TIME_UTC         = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static final String TODAY                 = "今天";

    public static final String YESTERDAY             = "昨天";

    public static final String TOMORROW              = "明天";

    private static Logger logger                = LoggerFactory.getLogger(DateUtils.class);

    /**
     * 时间戳转date
     * @param timestamp 1600758285059
     * @return
     */
    public static Date formatTimestamp(Long timestamp){
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Date date = ts;
        return date;
    }

    /**
     * 对日期增加指定的数目
     *
     * @param date
     * @param field
     * @param addNum
     * @return
     */
    public static Date dateAdd(Date date, int field, int addNum) {

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(field, addNum);
            return calendar.getTime();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Add specified number of days to the given date.
     * 
     * @param date date
     * @param days Int number of days to add
     * @return revised date
     */
    public static Date addDays(final Date date, int days) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);

        return new Date(cal.getTime().getTime());
    }

    public static Date addMins(final Date date, int mins) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, mins);

        return new Date(cal.getTime().getTime());
    }
    
    public static Date addSec(final Date date, int sec) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, sec);

        return new Date(cal.getTime().getTime());
    }

    /**
     * Add specified number of months to the date given.
     * 
     * @param date Date
     * @param months Int number of months to add
     * @return Date
     */
    public static Date addMonths(Date date, int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, months);
        return cal.getTime();
    }

    /**
     * Get date one day after specified one.
     * 
     * @param date1 Date 1
     * @param date2 Date 2
     * @return true if after day
     */
    public static boolean afterDay(final Date date1, final Date date2) {
        return getStartOfDate(date1).after(getStartOfDate(date2));
    }

    /**
     * Get date one day before specified one.
     * 
     * @param date1 test date
     * @param date2 date when
     * @return true if date2 is before date1
     */
    public static boolean beforeDay(final Date date1, final Date date2) {
        return getStartOfDate(date1).before(getStartOfDate(date2));
    }

    /**
     * 转换long类型到时,分,秒,毫秒的格式.
     * 
     * @param time long type
     * @return
     */
    public static String convert(long time) {
        long ms = time % 1000;
        time /= 1000;

        int h = Integer.valueOf("" + (time / 3600));
        int m = Integer.valueOf("" + ((time - h * 3600) / 60));
        int s = Integer.valueOf("" + (time - h * 3600 - m * 60));

        return h + "小时(H)" + m + "分(M)" + s + "秒(S)" + ms + "毫秒(MS)";
    }

    /**
     * 转换long类型到时,分,秒,毫秒的格式.
     * 
     * @param time long type
     * @return
     */
    public static String convertEn(long time) {
        long ms = time % 1000;
        time /= 1000;

        int h = Integer.valueOf("" + (time / 3600));
        int m = Integer.valueOf("" + ((time - h * 3600) / 60));
        int s = Integer.valueOf("" + (time - h * 3600 - m * 60));

        return h + "H" + m + "M" + s + "S" + ms + "MS";
    }

    /**
     * @param aDate
     * @return
     */
    public static String convertDateToString(String pattern, Date aDate) {
        return getDateTime(pattern, aDate);
    }

    /**
     * This method generates a string representation of a date/time in the format you specify on input
     * 
     * @param aMask the date pattern the string is in
     * @param strDate a string representation of a date
     * @return a converted Date object
     * @see SimpleDateFormat
     * @throws ParseException when String doesn't match the expected format
     */
    public static Date convertStringToDate(String aMask, String strDate) {
        SimpleDateFormat df;
        Date date = null;
        df = new SimpleDateFormat(aMask);

        if (logger.isDebugEnabled()) {
            logger.debug("converting '" + strDate + "' to date with mask '" + aMask + "'");
        }

        try {
            date = df.parse(strDate);
        } catch (ParseException pe) {
            logger.error("ParseException: " + pe);
        }

        return date;
    }

    /**
     * @return the current date without time component
     */
    public static Date currentDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Date(calendar.getTimeInMillis());
    }

    /**
     * Format date as "yyyy-MM-dd".
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDate(final Date date) {
        return formatDate(date, DEFAULT_PATTERN);
    }

    /**
     * Format date as given date format.
     *
     * @param date 日期
     * @return 格式化后的日期字符串yyyyMMdd
     */
    public static String formatDate2(final Date date) {
        return formatDate(date, DEFAULT_PATTERN2);
    }

    /**
     * Format date as given date format.
     *
     * @param dateTimeStr 日期格式的字符串
     * @return yyyy-MM-dd HH:mm:ss格式化后的日期字符串yyyyMMdd
     */
    public static String formatDate2(final String dateTimeStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_SHORT);
        Date date ;
        try {
            date = sdf.parse(dateTimeStr);
            sdf = new SimpleDateFormat(DEFAULT_PATTERN2);
            return  sdf.format(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Format date as given date format.
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDate(final Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }



    public static String formatDateTime(Date date) {
        return formatDate(date, DATE_TIME_SHORT);
    }

    public static String formatUTCTime(Date date){
        SimpleDateFormat df = new SimpleDateFormat(DATE_TIME_UTC);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(date);
    }

    public static String formatUTCTime2(Date date){
        SimpleDateFormat df = new SimpleDateFormat(FULL_PATTERN);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(date);
    }

    public static String formatUTCTime3(Date date){
        try {
            SimpleDateFormat df = new SimpleDateFormat(DATE_TIME_SHORT);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            return df.format(date);
        }catch (Exception e){

        }
        return StringUtils.EMPTY;

    }

    public static String formatUTCToLocalTime(String utcTime){
        SimpleDateFormat sdf = new SimpleDateFormat(FULL_PATTERN);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date utcDate;
        try {
            utcDate = sdf.parse(utcTime);
            sdf = new SimpleDateFormat(DATE_TIME_SHORT);
            sdf.setTimeZone(TimeZone.getDefault());
            String localTime = sdf.format(utcDate.getTime());
            return localTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Format date as "MM月dd日 HH:mm".
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatFullMatchDate(final Date date) {
        return formatDate(date, FULL_MATCH_PATTERN);
    }

    /**
     * 返回MM月dd日
     *
     * @param srcDate
     * @return
     */
    public static String formatMonthAndDay(Date srcDate) {
        Calendar cal1 = GregorianCalendar.getInstance();
        cal1.setTime(srcDate);
        SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日");

        return formatter.format(srcDate);
    }

    /**
     * 返回MM月dd日
     *
     * @param srcDate
     * @return
     */
    public static String formatMonthAndDay2(Date srcDate) {
        Calendar cal1 = GregorianCalendar.getInstance();
        cal1.setTime(srcDate);
        SimpleDateFormat formatter = new SimpleDateFormat("M月d日");

        return formatter.format(srcDate);
    }

    /**
     * 返回短日期格式
     *
     * @return [yyyy-mm-dd]
     */
    public static String formatShort(String strDate) {
        String ret = "";
        if (strDate != null && !"1900-01-01 00:00:00.0".equals(strDate) && strDate.indexOf("-") > 0) {
            ret = strDate;
            if (ret.indexOf(" ") > -1) {
                ret = ret.substring(0, ret.indexOf(" "));
            }
        }
        return ret;
    }

    /**
     * 格式化中文日期短日期格式
     *
     * @param gstrDate 输入欲格式化的日期
     * @return [yyyy年MM月dd日]
     */

    public static String formatShortDateC(Date gstrDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
        String pid = formatter.format(gstrDate);
        return pid;
    }

    /**
     * Format date as "HH:mm".
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatShortMatchDate(final Date date) {
        return formatDate(date, SHORT_MATCH_PATTERN);
    }

    /**
     * Format date as "HH:mm".
     *
     * @param dateTimeStr 日期
     * @return 格式化后的日期字符串
     */
    public static String formatShortMatchDate(final String dateTimeStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_SHORT);
        Date date;
        try {
            date = sdf.parse(dateTimeStr);
            return formatDate(date, SHORT_MATCH_PATTERN);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTimeStr;
    }

    /**
     * Format date as "yyyyMMdd".
     *
     * @param dateTimeStr 日期格式: 2019-04-17
     * @return 格式化后的日期字符串
     */
    public static String formatShortDate(final String dateTimeStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_PATTERN);
        Date date;
        try {
            date = sdf.parse(dateTimeStr);
            return formatDate(date, DEFAULT_PATTERN2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Format date as "HH:mm:ss".
     * @param dateTimeStr 日期
     * @return 格式化后的日期字符串
     */
    public static String formatShortSecondMatchDate(final String dateTimeStr) {
        if(StringUtils.isBlank(dateTimeStr)){
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_SHORT);
        Date date;
        try {
            date = sdf.parse(dateTimeStr);
            return formatDate(date, SHORT_SECOND_MATCH_PATTERN);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date getCurrentMonday() {
        Calendar cd = Calendar.getInstance();

        // 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
        int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK) - 1; // 因为按中国礼拜一作为第一天所以这里减1
        Date date;
        if (dayOfWeek == 1) {
            date = cd.getTime();
        } else {
            date = addDays(cd.getTime(), 1 - dayOfWeek);
        }

        return getStartOfDate(date);
    }

    /**
     * Return default datePattern (yyyy-MM-dd)
     *
     * @return a string representing the date pattern on the UI
     */
    public static String getDatePattern() {
        return "yyyy-MM-dd";
    }

    public static String getDateTime(Date date) {
        return formatDate(date, DATE_TIME_SHORT);
    }

    /**
     * This method generates a string representation of a date's date/time in the format you specify on input
     *
     * @param aMask the date pattern the string is in
     * @param aDate a date object
     * @return a formatted string representation of the date
     * @see SimpleDateFormat
     */
    public static String getDateTime(String aMask, Date aDate) {
        SimpleDateFormat df = null;
        String returnValue = "";

        if (aDate == null) {
            logger.error("aDate is null!");
        } else {
            df = new SimpleDateFormat(aMask);
            returnValue = df.format(aDate);
        }

        return (returnValue);
    }

    public static String getDateTimeFull(Date date) {
        return formatDate(date, DATE_TIME_FULL);
    }

    public static String getDateTimePattern() {
        return DateUtils.getDatePattern() + " HH:mm:ss.S";
    }

    /**
     * 返回当前日
     * 
     * @return [dd]
     */

    public static String getDay() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd");
        Date nowc = new Date();
        String pid = formatter.format(nowc);
        return pid;
    }

    /**
     * 一天的结束时间，【注：只精确到毫秒】
     * 
     * @param date
     * @return
     */
    public static Date getEndOfDate(final Date date) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);

        return new Date(cal.getTime().getTime());
    }

    /**
     * Return the end of the month based on the date passed as input parameter.
     * 
     * @param date Date
     * @return Date endOfMonth
     */
    public static Date getEndOfMonth(final Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
        calendar.set(Calendar.DATE, 0);

        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Date(calendar.getTimeInMillis());
    }

    /**
     * Get first day of month.
     * 
     * @param date Date
     * @return Date
     */
    public static Date getFirstOfMonth(final Date date) {
        Date lastMonth = addMonths(date, -1);
        lastMonth = getEndOfMonth(lastMonth);
        return addDays(lastMonth, 1);
    }

    public static Date getMondayBefore4Week() {
        Calendar cd = Calendar.getInstance();

        // 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
        int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK) - 1; // 因为按中国礼拜一作为第一天所以这里减1
        Date date;
        if (dayOfWeek == 1) {
            date = addDays(cd.getTime(), -28);
        } else {
            date = addDays(cd.getTime(), -27 - dayOfWeek);
        }

        return getStartOfDate(date);
    }

    /**
     * 返回当前月份
     * 
     * @return [MM]
     */

    public static String getMonth() {
        SimpleDateFormat formatter = new SimpleDateFormat("MM");
        Date nowc = new Date();
        String pid = formatter.format(nowc);
        return pid;
    }

    /**
     * 返回标准格式的当前时间
     * 
     * @return [yyyy-MM-dd k:mm:ss]
     */

    public static String getNow() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd H:mm:ss");
        Date nowc = new Date();
        String pid = formatter.format(nowc);
        return pid;
    }

    /**
     * 计算2个日前直接相差的天数
     * 
     * @param cal1
     * @param cal2
     * @return
     */
    public static long getNumberOfDaysBetween(Calendar cal1, Calendar cal2) {
        cal1.clear(Calendar.MILLISECOND);
        cal1.clear(Calendar.SECOND);
        cal1.clear(Calendar.MINUTE);
        cal1.clear(Calendar.HOUR_OF_DAY);

        cal2.clear(Calendar.MILLISECOND);
        cal2.clear(Calendar.SECOND);
        cal2.clear(Calendar.MINUTE);
        cal2.clear(Calendar.HOUR_OF_DAY);

        long elapsed = cal2.getTime().getTime() - cal1.getTime().getTime();
        return elapsed / DAY;
    }

    /**
     * 返回两个时间间隔的天数
     * 
     * @param before 起始时间
     * @param end 终止时间
     * @return 天数
     */
    public static long getNumberOfDaysBetween(final Date before, final Date end) {
        long millisec = end.getTime() - before.getTime() + 1;
        return millisec / DAY;
    }

    /**
     * 返回两个时间间隔的小时数
     * 
     * @param before 起始时间
     * @param end 终止时间
     * @return 小时数
     */
    public static long getNumberOfHoursBetween(final Date before, final Date end) {
        long millisec = end.getTime() - before.getTime() + 1;
        return millisec / (60 * 60 * 1000);
    }

    /**
     * 返回两个时间间隔的分钟数
     * 
     * @param before 起始时间
     * @param end 终止时间
     * @return 分钟数
     */
    public static long getNumberOfMinuteBetween(final Date before, final Date end) {
        long millisec = end.getTime() - before.getTime();
        return millisec / (60 * 1000);
    }

    public static int getNumberOfSecondsBetween(final Date before, final Date end) {
        return (int) (Math.abs(end.getTime() - before.getTime()) / SECOND);
    }

    public static int getNumberOfMonthsBetween(final Date before, final Date end) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(before);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(end);
        return (cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR)) * 12
               + (cal2.get(Calendar.MONTH) - cal1.get(Calendar.MONTH));
    }

    public static int getNumberOfSecondsBetween(final double end, final double start) {
        if ((end == 0) || (start == 0)) {
            return -1;
        }

        return (int) (Math.abs(end - start) / SECOND);
    }

    public static Date getPreviousMonday() {
        Calendar cd = Calendar.getInstance();

        // 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
        int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK) - 1; // 因为按中国礼拜一作为第一天所以这里减1
        Date date;
        if (dayOfWeek == 1) {
            date = addDays(cd.getTime(), -7);
        } else {
            date = addDays(cd.getTime(), -6 - dayOfWeek);
        }

        return getStartOfDate(date);
    }

    /**
     * 返回中文格式的当前日期
     * 
     * @return [yyyy-mm-dd]
     */
    public static String getShortNow() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date nowc = new Date();
        String pid = formatter.format(nowc);
        return pid;
    }

    /**
     * Get start of date.
     * 
     * @param date Date
     * @return Date Date
     */
    public static Date getStartOfDate(final Date date) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return new Date(cal.getTime().getTime());
    }

    /**
     * 返回当前时间24小时制式
     * 
     * @return [H:mm]
     */

    public static String getTimeBykm() {
        SimpleDateFormat formatter = new SimpleDateFormat("H:mm");
        Date nowc = new Date();
        String pid = formatter.format(nowc);
        return pid;
    }

    public static Date getToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Date(calendar.getTimeInMillis());
    }

    public static Date setSecond(Date date, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Date(calendar.getTimeInMillis());
    }

    /**
     * 检查日期的合法性
     * 
     * @param sourceDate
     * @return
     */
    public static boolean inFormat(String sourceDate, String format) {
        if (sourceDate == null) {
            return false;
        }
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setLenient(false);
            dateFormat.parse(sourceDate);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = GregorianCalendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = GregorianCalendar.getInstance();
        cal2.setTime(date2);

        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
               && (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) && (cal1.get(Calendar.DATE) == cal2.get(Calendar.DATE)));
    }

    /**
     * Compare the two calendars whether they are in the same month.
     * 
     * @param cal1 the first calendar
     * @param cal2 the second calendar
     * @return whether are in the same month
     */
    public static boolean isSameMonth(Calendar cal1, Calendar cal2) {
        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
               && (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH));
    }

    /**
     * 获取指定年月的指定的某天某一时刻,比如year=2018,month=12,day=1,hour=23,minute=59,second=59,返回2018年12月1日23点59分59秒
     * 这里
     * @param year
     * @param day
     * @return
     */
    public static Date getDate(int year, int month, int day,int hour, int minute, int second){
        Calendar calendar = Calendar.getInstance();
        if((year > 0 )&&(month >=1)) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month-1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, second);
            return calendar.getTime();
        }
        return null;
    }

    /**
     * Compare the two dates whether are in the same month.
     * 
     * @param date1 the first date
     * @param date2 the second date
     * @return whether are in the same month
     */
    public static boolean isSameMonth(Date date1, Date date2) {
        Calendar cal1 = GregorianCalendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = GregorianCalendar.getInstance();
        cal2.setTime(date2);
        return isSameMonth(cal1, cal2);
    }

    /**
     * date转为整型yyyyMMdd格式日期
     * @param date
     * @return
     */
    public static Long getLongDs(Date date){
        if(null == date){
            return null;
        }
        String stringDs = DateUtils.formatDate(date,"yyyyMMdd");
        Long longDs = Long.valueOf(stringDs);
        return longDs;
    }

    public static String getYestTodayTomTitle(Long ds){
        if(null == ds || ds <=0){
            return StringUtils.EMPTY;
        }
        Date todayDate = nowDate();
        Long todayDs = getLongDs(todayDate);
        if(ds.longValue() == todayDs){
            return TODAY;
        }
        Date date = parseDate(String.valueOf(ds), DEFAULT_PATTERN2);
        Date yesterdayDate = addDays(todayDate, -1);
        Long yesterdayDs = getLongDs(yesterdayDate);
        if(ds.longValue() == yesterdayDs){
            return YESTERDAY;
        }
        Date tomorrowDate = addDays(todayDate, 1);
        Long tomorrowDs = getLongDs(tomorrowDate);
        if(ds.longValue() == tomorrowDs){
            return TOMORROW;
        }
        return formatDate(date, MONTH_DAY_PATTERN);
    }

    /**
     * 判断当前hhMm时刻的值是否在指定的开始和结束时间内
     * @param startHhmm  20:10
     * @param endHhmm   20:20
     * @param hhMm      20:02
     * @return
     */
    public static boolean isContainHhMm(String startHhmm, String endHhmm, String hhMm){
        if(StringUtils.isBlank(startHhmm) || StringUtils.isBlank(endHhmm) || StringUtils.isBlank(hhMm)){
            return false;
        }
        try {
            Long start = Long.valueOf(startHhmm.replace(":",""));
            Long end = Long.valueOf(endHhmm.replace(":",""));
            Long hM = Long.valueOf(hhMm.replace(":",""));
            if(start <= end){//没有跨天
                if(hM>=start && hM<=end){
                    return true;
                }
            }else{//跨天
                if(hM >= start){
                    return true;
                }else if(hM <= end){
                    return true;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断指定的节目播单的结束时间date endHhmm日期是否小于于当前时刻(仅给节目播单开始和结束时间使用)
     * @param date
     * @param startHhmm
     * @param endHhmm
     * @return true:小于当前时刻
     */
    public static boolean isBeforeNow(Date date, String startHhmm, String endHhmm){
        if(StringUtils.isBlank(startHhmm) || StringUtils.isBlank(endHhmm) || null == date){
            return false;
        }
        try {
            System.out.println(date);
            Long start = Long.valueOf(startHhmm.replace(":",""));
            Long end = Long.valueOf(endHhmm.replace(":",""));
            if(start <= end){//没有跨天
                Date newDate = parseDate(formatDate(date) + " " + endHhmm , DATE_TIME_MINUTE);
                return getNumberOfMinuteBetween(newDate, new Date()) > 0L;
            }else{//跨天
                Date newDate = parseDate(formatDate(DateUtils.addDays(date,1)) + " " + endHhmm , DATE_TIME_MINUTE);
                return getNumberOfMinuteBetween(newDate, new Date()) > 0L;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }



    /**
     * get date time as "yyyyMMddhhmmss"
     * 
     * @return the current date with time component
     */
    public static String now() {
        return formatDate(new Date(), FULL_PATTERN);
    }

    public static Date nowDate() {
        return new Date();
    }
    /**
     * change the string to date
     * 
     * @param
     * @return Date if failed return <code>null</code>
     */
    public static Date parseDate(String date) {
        return parseDate(date, DEFAULT_PATTERN, null);
    }

    /**
     * change the string to date
     * 
     * @param date
     * @param df
     * @return Date
     */
    public static Date parseDate(String date, String df) {
        if(StringUtils.isBlank(date) || StringUtils.isBlank(df)){
            return null;
        }
        return parseDate(date, df, null);
    }

    /**
     * change the string to date
     * 
     * @param date
     * @param df DateFormat
     * @param defaultValue if parse failed return the default value
     * @return Date
     */
    public static Date parseDate(String date, String df, Date defaultValue) {
        if (date == null) {
            return defaultValue;
        }

        SimpleDateFormat formatter = new SimpleDateFormat(df);

        try {
            return formatter.parse(date);
        } catch (ParseException e) {
        }

        return defaultValue;
    }

    private DateUtils() {

    }

    /**
     * 获取当前时间相差hours小时的开始小时时间
     * @param hours
     * @return String format of "yyyyMMddHHmmss"
     */
    public static String addStartHour(int hours){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + hours);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH");
        return df.format(calendar.getTime()) + "0000";
    }


    /**
     * 判断相差的月份
     * @param startDate  起始月份
     * @param endDate  结束月份
     * @return
     */
    public static int monthsBetween(Date startDate, Date endDate) {
        Calendar startCalendar = new GregorianCalendar();
        startCalendar.setTime(startDate);
        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(endDate);

        int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
        return diffMonth;
    }

    public static long secondsFromMonthBegin(Date date) {
        return (date.getDate()-1)*3600*24+date.getHours()*3600+date.getMinutes()*60+date.getSeconds();
    }

    public static void main(String[] args) throws Exception{
        System.out.println(formatTimestamp(1600758285059L));

    }
}

