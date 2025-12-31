package com.example.mycalendarapp.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.ibm.icu.util.ChineseCalendar;

public class DateUtils {

    // 格式化日期为 "yyyy年MM月dd日"
    public static String formatDate(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
        return sdf.format(new Date(timeInMillis));
    }

    // 格式化时间为 "HH:mm"
    public static String formatTime(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timeInMillis));
    }

    // 获取当天的开始时间 (00:00:00)
    public static long getStartOfDay(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    // 获取当天的结束时间 (23:59:59)
    public static long getEndOfDay(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    // --- 真实农历功能实现 ---

    /**
     * 获取完整的农历日期字符串（使用ICU4J库）
     * 格式："农历yyyy年MM月dd日"
     */
    public static String getLunarDate(int year, int month, int day) {
        try {
            // 创建公历日历并设置日期
            Calendar gregorianCal = Calendar.getInstance();
            gregorianCal.set(year, month - 1, day); // month is 0-based in Calendar

            // 创建农历日历并同步时间
            ChineseCalendar lunarCal = new ChineseCalendar();
            lunarCal.setTime(gregorianCal.getTime());

            // 设置中文语言环境
            Locale chineseLocale = Locale.SIMPLIFIED_CHINESE;

            // 使用ICU的SimpleDateFormat格式化农历日期
            com.ibm.icu.text.SimpleDateFormat lunarSdf = new com.ibm.icu.text.SimpleDateFormat("农历yyyy年MM月dd日", chineseLocale);
            lunarSdf.setCalendar(lunarCal);

            // 获取格式化的农历日期
            String lunarDate = lunarSdf.format(lunarCal);

            // 替换数字月份为中文月份（如"01"->"正月"）
            lunarDate = replaceLunarMonth(lunarDate);

            return lunarDate;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取农历月份的第一天信息
     * @return true 如果是农历月份的第一天
     */
    public static boolean isLunarMonthFirstDay(int year, int month, int day) {
        try {
            // 创建公历日历并设置日期
            Calendar gregorianCal = Calendar.getInstance();
            gregorianCal.set(year, month - 1, day); // month is 0-based in Calendar

            // 创建农历日历并同步时间
            ChineseCalendar lunarCal = new ChineseCalendar();
            lunarCal.setTime(gregorianCal.getTime());

            // 判断是否是农历月份的第一天
            return lunarCal.get(ChineseCalendar.DAY_OF_MONTH) == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取简短的农历日期字符串
     * 格式："正月初一"
     */
    public static String getShortLunarDate(int year, int month, int day) {
        try {
            // 创建公历日历并设置日期
            Calendar gregorianCal = Calendar.getInstance();
            gregorianCal.set(year, month - 1, day); // month is 0-based in Calendar

            // 创建农历日历并同步时间
            ChineseCalendar lunarCal = new ChineseCalendar();
            lunarCal.setTime(gregorianCal.getTime());

            // 获取农历年、月、日
            int lunarMonth = lunarCal.get(ChineseCalendar.MONTH) + 1; // 转换为1-based
            int lunarDay = lunarCal.get(ChineseCalendar.DAY_OF_MONTH);
            boolean isLeapMonth = lunarCal.get(ChineseCalendar.IS_LEAP_MONTH) == 1;

            // 生成简短的农历日期字符串
            String shortLunarDate = (isLeapMonth ? "闰" : "") + getChineseMonth(lunarMonth) + getChineseDay(lunarDay);

            return shortLunarDate;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取农历月份名称
     */
    public static String getLunarMonthName(int year, int month, int day) {
        try {
            // 创建公历日历并设置日期
            Calendar gregorianCal = Calendar.getInstance();
            gregorianCal.set(year, month - 1, day); // month is 0-based in Calendar

            // 创建农历日历并同步时间
            ChineseCalendar lunarCal = new ChineseCalendar();
            lunarCal.setTime(gregorianCal.getTime());

            // 获取农历月份
            int lunarMonth = lunarCal.get(ChineseCalendar.MONTH) + 1; // 转换为1-based
            boolean isLeapMonth = lunarCal.get(ChineseCalendar.IS_LEAP_MONTH) == 1;

            // 生成农历月份名称
            return (isLeapMonth ? "闰" : "") + getChineseMonth(lunarMonth);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取农历日期名称（不包含月份）
     */
    public static String getLunarDayName(int year, int month, int day) {
        try {
            // 创建公历日历并设置日期
            Calendar gregorianCal = Calendar.getInstance();
            gregorianCal.set(year, month - 1, day); // month is 0-based in Calendar

            // 创建农历日历并同步时间
            ChineseCalendar lunarCal = new ChineseCalendar();
            lunarCal.setTime(gregorianCal.getTime());

            // 获取农历日期
            int lunarDay = lunarCal.get(ChineseCalendar.DAY_OF_MONTH);

            // 生成农历日期名称
            return getChineseDay(lunarDay);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 替换数字月份为中文月份
     */
    private static String replaceLunarMonth(String lunarDate) {
        // 农历月份中文名称
        String[] monthNames = {"正月", "二月", "三月", "四月", "五月", "六月",
                "七月", "八月", "九月", "十月", "冬月", "腊月"};

        // 替换数字月份
        for (int i = 0; i < monthNames.length; i++) {
            lunarDate = lunarDate.replace((i + 1) < 10 ? "0" + (i + 1) : "" + (i + 1), monthNames[i]);
        }

        return lunarDate;
    }

    /**
     * 获取中文月份名称
     */
    private static String getChineseMonth(int month) {
        String[] monthNames = {"正月", "二月", "三月", "四月", "五月", "六月",
                "七月", "八月", "九月", "十月", "冬月", "腊月"};
        return month >= 1 && month <= 12 ? monthNames[month - 1] : "";
    }

    /**
     * 获取中文日期名称
     */
    private static String getChineseDay(int day) {
        if (day < 1 || day > 30) return "";

        String[] dayNames = {"初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
                "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
                "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"};
        return dayNames[day - 1];
    }

    /**
     * 获取农历年份的生肖
     */
    public static String getChineseZodiac(int year) {
        String[] zodiacs = {"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
        int zodiacIndex = (year - 1900) % 12;
        if (zodiacIndex < 0) zodiacIndex += 12;
        return zodiacs[zodiacIndex];
    }

    /**
     * 从时间戳获取农历日期
     */
    public static String getLunarDate(long timeInMillis) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timeInMillis);
            return getLunarDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 从时间戳获取简短农历日期
     */
    public static String getShortLunarDate(long timeInMillis) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timeInMillis);
            return getShortLunarDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}