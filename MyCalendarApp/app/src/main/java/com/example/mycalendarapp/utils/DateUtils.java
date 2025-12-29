package com.example.mycalendarapp.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

    // --- 扩展要求：农历相关实现 ---

    /**
     * 获取简化的农历字符串
     * 注意：完整的农历算法代码量很大，这里为了课程设计演示，
     * 使用一个简化的模拟逻辑，或者你可以引入开源库如 'ChineseCalendar'
     */
    public static String getLunarDate(int year, int month, int day) {
        // 实际开发中，这里通常会调用一个包含1900-2100年数据的 Lunar 类
        // 下面是一个返回演示文本的示例逻辑

        // 模拟逻辑：假设1月1日是农历正月初一（实际不是）
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day); // month is 0-based in Calendar

        // 这里为了演示，我们硬编码几个节日，真实逻辑需查表
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);

        if (month == 1 && day == 1) return "元旦";
        if (month == 2 && day == 14) return "情人";

        // 这里的返回值只是示例，实际需要复杂的农历计算类
        // 如果你想省事，可以返回 "农历" + day;
        return "腊" + (30 - day); // 仅作演示数据
    }

    /**
     * (推荐) 真实的农历转换参考逻辑
     * 如果要实现完整功能，请创建一个 Lunar 类，包含 LunarYearData 数组
     * 然后通过位运算计算。鉴于篇幅，这里提供接口设计。
     */
    public static String getChineseDate(Calendar calendar) {
        // 调用外部库或者自己实现的 LunarCalendar.convert(calendar)
        return "农历示例";
    }
}
