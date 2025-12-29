package com.example.mycalendarapp;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mycalendarapp.adapter.CalendarAdapter;
import com.example.mycalendarapp.adapter.EventAdapter;
import com.example.mycalendarapp.database.DatabaseHelper;
import com.example.mycalendarapp.model.Event;
import com.example.mycalendarapp.receiver.AlarmReceiver;
import com.example.mycalendarapp.utils.DateUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener {
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private RecyclerView eventRecyclerView;
    private FloatingActionButton addEventFAB;
    private Button prevBtn, nextBtn;
    private EventAdapter eventAdapter;

    private Calendar calendar = Calendar.getInstance();
    private DatabaseHelper dbHelper;
    private int selectedPosition = -1;
    private ArrayList<String> daysInMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        dbHelper = new DatabaseHelper(this);
        checkPermissions();
        setMonthView();

        // 月份切换监听
        prevBtn.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            setMonthView();
        });

        nextBtn.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            setMonthView();
        });

        // 添加日程按钮
        addEventFAB.setOnClickListener(v -> {
            Calendar selectedDate = Calendar.getInstance();
            if (selectedPosition != -1 && daysInMonth != null) {
                String dayText = daysInMonth.get(selectedPosition);
                if (!dayText.isEmpty()) {
                    try {
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = Integer.parseInt(dayText);
                        selectedDate.set(year, month, day);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "日期格式错误", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
            showAddEventDialog(selectedDate);
        });
    }

    private void initViews() {
        monthYearText = findViewById(R.id.monthYearText);
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        eventRecyclerView = findViewById(R.id.eventRecyclerView);
        addEventFAB = findViewById(R.id.addEventFAB);
        prevBtn = findViewById(R.id.prevBtn);
        nextBtn = findViewById(R.id.nextBtn);

        eventAdapter = new EventAdapter(new ArrayList<>());
        eventAdapter.setOnItemClickListener(this::showViewEventDialog);
        eventAdapter.setOnItemLongClickListener(this::showEditDeleteDialog);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventRecyclerView.setAdapter(eventAdapter);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    private void setMonthView() {
        monthYearText.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.getTime()));

        daysInMonth = daysInMonthArray(calendar);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this, calendar);
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
        calendarAdapter.setSelectedPosition(-1);
    }

    private ArrayList<String> daysInMonthArray(Calendar calendar) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, 1);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeek == 0) dayOfWeek = 7;

        for (int i = 1; i < dayOfWeek; i++) {
            daysInMonthArray.add("");
        }

        for (int i = 1; i <= daysInMonth; i++) {
            daysInMonthArray.add(String.valueOf(i));
        }

        return daysInMonthArray;
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.isEmpty()) {
            try {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = Integer.parseInt(dayText);

                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, day);

                CalendarAdapter adapter = (CalendarAdapter) calendarRecyclerView.getAdapter();
                if (adapter != null) {
                    adapter.setSelectedPosition(position);
                    this.selectedPosition = position;
                }

                long startOfDay = DateUtils.getStartOfDay(selectedDate.getTimeInMillis());
                long endOfDay = DateUtils.getEndOfDay(selectedDate.getTimeInMillis());
                List<Event> events = dbHelper.getEventsByDay(startOfDay, endOfDay);
                eventAdapter.setEvents(events);

                if (events.isEmpty()) {
                    Toast.makeText(this, "今日无安排", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "日期解析错误", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "加载日程失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAddEventDialog(Calendar selectedDate) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("添加日程 - " + DateUtils.formatDate(selectedDate.getTimeInMillis()));

            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_event, null);
            builder.setView(dialogView);

            final EditText titleInput = dialogView.findViewById(R.id.eventTitleInput);
            final TextView dateDisplay = dialogView.findViewById(R.id.dateDisplay);
            final Button startTimeBtn = dialogView.findViewById(R.id.startTimeBtn);
            final Button endTimeBtn = dialogView.findViewById(R.id.endTimeBtn);

            dateDisplay.setText(DateUtils.formatDate(selectedDate.getTimeInMillis()));

            // 使用选中的日期作为基础，而不是当前时间
            final Calendar startTime = (Calendar) selectedDate.clone();
            final Calendar endTime = (Calendar) selectedDate.clone();
            endTime.add(Calendar.HOUR_OF_DAY, 1);

            // 设置默认时间为当前时间
            Calendar now = Calendar.getInstance();
            startTime.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
            startTime.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
            endTime.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) + 1);
            endTime.set(Calendar.MINUTE, now.get(Calendar.MINUTE));

            startTimeBtn.setText(DateUtils.formatTime(startTime.getTimeInMillis()));
            endTimeBtn.setText(DateUtils.formatTime(endTime.getTimeInMillis()));

            startTimeBtn.setOnClickListener(v -> showTimePickerDialog(startTime, time -> {
                startTimeBtn.setText(time);
                if (endTime.before(startTime)) {
                    endTime.setTimeInMillis(startTime.getTimeInMillis() + 3600000);
                    endTimeBtn.setText(DateUtils.formatTime(endTime.getTimeInMillis()));
                }
            }));

            endTimeBtn.setOnClickListener(v -> showTimePickerDialog(endTime, time -> {
                endTimeBtn.setText(time);
            }));

            builder.setPositiveButton("确定", (dialog, which) -> {
                String title = titleInput.getText().toString().trim();
                if (title.isEmpty()) {
                    Toast.makeText(this, "请输入日程内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    // 使用选中的日期和时间
                    Event event = new Event(
                            title,
                            "Default Description",
                            startTime.getTimeInMillis(),
                            endTime.getTimeInMillis(),
                            startTime.getTimeInMillis()
                    );

                    long eventId = dbHelper.addEvent(event);
                    if (eventId != -1) {
                        setAlarm(event);
                        Toast.makeText(this, "日程已添加", Toast.LENGTH_SHORT).show();
                        // 刷新当前选中日期的日程
                        onItemClick(selectedPosition, daysInMonth.get(selectedPosition));
                    } else {
                        Toast.makeText(this, "添加失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "创建日程失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });

            builder.setNegativeButton("取消", null);
            builder.show();
        } catch (Exception e) {
            Toast.makeText(this, "打开添加日程对话框失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // 修复：添加调试信息并放宽时间检查
    private void setAlarm(Event event) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Toast.makeText(this, "无法获取闹钟服务", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("eventTitle", event.getTitle());

        // 添加调试信息
        int eventId = event.getId();
        long remindTime = event.getRemindTime();
        long currentTime = System.currentTimeMillis();

        // 调试输出
        android.util.Log.d("AlarmSetup", "Event ID: " + eventId);
        android.util.Log.d("AlarmSetup", "Remind Time: " + remindTime);
        android.util.Log.d("AlarmSetup", "Current Time: " + currentTime);
        android.util.Log.d("AlarmSetup", "Time Difference: " + (remindTime - currentTime) + "ms");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                eventId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // 放宽时间检查：允许设置过去1小时内的闹钟（用于测试）
        if (remindTime > currentTime - 3600000) {  // 允许过去1小时内的闹钟
            try {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        remindTime,
                        pendingIntent
                );
                android.util.Log.d("AlarmSetup", "闹钟设置成功");
            } catch (Exception e) {
                android.util.Log.e("AlarmSetup", "设置闹钟失败: " + e.getMessage());
                Toast.makeText(this, "设置闹钟失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            android.util.Log.d("AlarmSetup", "提醒时间在过去，不设置闹钟");
        }
    }

    private void showTimePickerDialog(Calendar calendar, TimeSetListener listener) {
        try {
            TimePickerDialog timePicker = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        String time = String.format("%02d:%02d", hourOfDay, minute);
                        listener.onTimeSet(time);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePicker.show();
        } catch (Exception e) {
            Toast.makeText(this, "打开时间选择器失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showViewEventDialog(Event event) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("日程详情");

            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_view_event, null);
            builder.setView(dialogView);

            TextView titleView = dialogView.findViewById(R.id.eventTitle);
            TextView dateView = dialogView.findViewById(R.id.eventDate);
            TextView timeView = dialogView.findViewById(R.id.eventTime);
            TextView descView = dialogView.findViewById(R.id.eventDesc);

            titleView.setText(event.getTitle());
            dateView.setText(DateUtils.formatDate(event.getStartTime()));
            timeView.setText(DateUtils.formatTime(event.getStartTime()) + " - " + DateUtils.formatTime(event.getEndTime()));
            descView.setText(event.getDescription());

            builder.setPositiveButton("关闭", null);
            builder.show();
        } catch (Exception e) {
            Toast.makeText(this, "显示日程详情失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showEditDeleteDialog(Event event, int position) {
        try {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("操作")
                    .setItems(new String[]{"编辑", "删除"}, (dialog, which) -> {
                        if (which == 0) {
                            showEditEventDialog(event);
                        } else if (which == 1) {
                            showDeleteEventDialog(event);
                        }
                    })
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "显示操作菜单失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showEditEventDialog(Event event) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("编辑日程");

            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_event, null);
            builder.setView(dialogView);

            final EditText titleInput = dialogView.findViewById(R.id.eventTitleInput);
            final TextView dateDisplay = dialogView.findViewById(R.id.dateDisplay);
            final Button startTimeBtn = dialogView.findViewById(R.id.startTimeBtn);
            final Button endTimeBtn = dialogView.findViewById(R.id.endTimeBtn);

            titleInput.setText(event.getTitle());
            dateDisplay.setText(DateUtils.formatDate(event.getStartTime()));

            Calendar startTime = Calendar.getInstance();
            startTime.setTimeInMillis(event.getStartTime());
            Calendar endTime = Calendar.getInstance();
            endTime.setTimeInMillis(event.getEndTime());

            startTimeBtn.setText(DateUtils.formatTime(startTime.getTimeInMillis()));
            endTimeBtn.setText(DateUtils.formatTime(endTime.getTimeInMillis()));

            startTimeBtn.setOnClickListener(v -> showTimePickerDialog(startTime, time -> {
                startTimeBtn.setText(time);
                if (endTime.before(startTime)) {
                    endTime.setTimeInMillis(startTime.getTimeInMillis() + 3600000);
                    endTimeBtn.setText(DateUtils.formatTime(endTime.getTimeInMillis()));
                }
            }));

            endTimeBtn.setOnClickListener(v -> showTimePickerDialog(endTime, time -> {
                endTimeBtn.setText(time);
            }));

            builder.setPositiveButton("保存", (dialog, which) -> {
                String title = titleInput.getText().toString().trim();
                if (title.isEmpty()) {
                    Toast.makeText(this, "请输入日程内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    event.setTitle(title);
                    event.setStartTime(startTime.getTimeInMillis());
                    event.setEndTime(endTime.getTimeInMillis());
                    event.setRemindTime(startTime.getTimeInMillis());

                    if (dbHelper.updateEvent(event)) {
                        updateAlarm(event);
                        Toast.makeText(this, "日程已更新", Toast.LENGTH_SHORT).show();
                        onItemClick(selectedPosition, daysInMonth.get(selectedPosition));
                    } else {
                        Toast.makeText(this, "更新失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "更新日程失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });

            builder.setNegativeButton("取消", null);
            builder.show();
        } catch (Exception e) {
            Toast.makeText(this, "打开编辑日程对话框失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showDeleteEventDialog(Event event) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("删除日程")
                    .setMessage("确定要删除这个日程吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        try {
                            int eventId = event.getId();
                            boolean deleteResult = dbHelper.deleteEvent(eventId);

                            if (deleteResult) {
                                cancelAlarm(event);
                                Toast.makeText(this, "日程已删除", Toast.LENGTH_SHORT).show();
                                onItemClick(selectedPosition, daysInMonth.get(selectedPosition));
                            } else {
                                Toast.makeText(this, "删除失败，请重试", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, "删除日程失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "显示删除确认对话框失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void updateAlarm(Event event) {
        cancelAlarm(event);
        setAlarm(event);
    }

    private void cancelAlarm(Event event) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                event.getId(),
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        alarmManager.cancel(pendingIntent);
    }

    public interface TimeSetListener {
        void onTimeSet(String time);
    }
}


