package com.example.mycalendarapp.model;

import java.io.Serializable;

public class Event implements Serializable {
    private int id;
    private String title;       // 日程标题 (SUMMARY in RFC5545)
    private String description; // 描述 (DESCRIPTION)
    private long startTime;     // 开始时间戳 (DTSTART)
    private long endTime;       // 结束时间戳 (DTEND)
    private long remindTime;    // 提醒时间戳

    // 构造函数
    public Event(String title, String description, long startTime, long endTime, long remindTime) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.remindTime = remindTime;
    }

    // Setter方法
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setRemindTime(long remindTime) {
        this.remindTime = remindTime;
    }

    // Getter 和 Setter 方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public long getRemindTime() { return remindTime; }
}
