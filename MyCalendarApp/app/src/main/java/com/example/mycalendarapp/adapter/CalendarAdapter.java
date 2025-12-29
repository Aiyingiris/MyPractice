package com.example.mycalendarapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mycalendarapp.R;
import java.util.ArrayList;
import java.util.Calendar;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {
    private ArrayList<String> daysOfMonth;
    private OnItemListener onItemListener;
    private int selectedPosition = -1; // 记录选中的位置
    private int todayPosition = -1;    // 记录今天的位置
    private Calendar currentCalendar;  // 当前显示的日历实例

    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener, Calendar calendar) {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        this.currentCalendar = (Calendar) calendar.clone(); // 保存当前日历实例
        calculateTodayPosition();
    }


    // 计算今天在日历中的位置
    private void calculateTodayPosition() {
        Calendar today = Calendar.getInstance();
        Calendar displayCalendar = (Calendar) currentCalendar.clone(); // 当前显示的日历

        // 检查今天是否在当前显示的月份中
        if (today.get(Calendar.YEAR) == displayCalendar.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == displayCalendar.get(Calendar.MONTH)) {
            // 今天在当前显示的月份中，计算位置
            displayCalendar.set(Calendar.DAY_OF_MONTH, 1);
            int firstDayOfWeek = displayCalendar.get(Calendar.DAY_OF_WEEK);
            int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);
            int offset = firstDayOfWeek == Calendar.SUNDAY ? 0 : firstDayOfWeek - 1;
            todayPosition = offset + dayOfMonth - 2;
        } else {
            // 今天不在当前显示的月份中，不显示高亮
            todayPosition = -1;
        }
    }

    // 更新当前日历实例
    public void updateCalendar(Calendar calendar) {
        this.currentCalendar = (Calendar) calendar.clone();
        calculateTodayPosition();
        notifyDataSetChanged();
    }
    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_calendar_grid, parent, false);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        String dayText = daysOfMonth.get(position);
        holder.dayOfMonth.setText(dayText);

        // 处理周天和周内
        if (position % 7 == 0) {
            holder.dayOfMonth.setTextColor(Color.RED); // 周日标红
        } else {
            holder.dayOfMonth.setTextColor(Color.BLACK);
        }
        // 设置不同的文本颜色
        if (dayText.isEmpty()) {
            holder.dayOfMonth.setTextColor(Color.TRANSPARENT);
            holder.dayOfMonth.setBackgroundResource(0);
        } else {
            // 检查是否是今天
            boolean isToday = position == todayPosition;
            // 检查是否是选中的日期
            boolean isSelected = position == selectedPosition;

            if (isSelected) {
                // 选中日期使用深蓝色
                holder.dayOfMonth.setTextColor(Color.WHITE);
                holder.dayOfMonth.setBackgroundResource(R.drawable.selected_background);
            } else if (isToday) {
                // 今天使用浅蓝色（但不是选中状态）
                holder.dayOfMonth.setTextColor(Color.BLUE);
                holder.dayOfMonth.setBackgroundResource(R.drawable.today_background);
            } else {
                // 其他日期普通显示
                holder.dayOfMonth.setTextColor(Color.BLACK);
                holder.dayOfMonth.setBackgroundResource(0);
            }
        }
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    public interface OnItemListener {
        void onItemClick(int position, String dayText);
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView dayOfMonth;
        private OnItemListener onItemListener;

        public CalendarViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);
            dayOfMonth = itemView.findViewById(R.id.cellDayText);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemListener.onItemClick(getAdapterPosition(), (String) dayOfMonth.getText());
        }
    }
}

