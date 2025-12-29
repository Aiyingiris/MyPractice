package com.example.mycalendarapp.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.mycalendarapp.MainActivity;
import com.example.mycalendarapp.R;
import com.example.mycalendarapp.model.Event;
import java.util.Objects;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "CalendarApp_Channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 从 Intent 中获取日程标题
        String eventTitle = intent.getStringExtra("eventTitle");
        if (eventTitle == null) eventTitle = "未知日程";

        // 创建通知渠道（Android 8.0+ 必须）
        createNotificationChannel(context);

        // 点击通知后跳转到 MainActivity
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // 使用应用图标（确保存在）
                .setContentTitle("日程提醒")
                .setContentText(eventTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 使用时间戳作为唯一ID，防止通知覆盖
        long notificationId = System.currentTimeMillis();
        if (manager != null) {
            manager.notify((int) notificationId, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Calendar Reminder Channel";
            String description = "Channel for calendar event reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}


