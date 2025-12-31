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
        
        // 设置不同的文本颜色和背景
        if (dayText.isEmpty()) {
            // 非当前月份的日期
            holder.dayOfMonth.setTextColor(Color.TRANSPARENT);
            holder.dayOfMonth.setBackgroundResource(0);
            holder.lunarDayOfMonth.setText(""); // 不显示农历
        } else {
            try {
                // 计算对应的农历日期
                int day = Integer.parseInt(dayText);
                int year = currentCalendar.get(Calendar.YEAR);
                int month = currentCalendar.get(Calendar.MONTH) + 1; // 转换为1-based
                
                // 检查是否是农历月份的第一天
                boolean isLunarMonthFirstDay = com.example.mycalendarapp.utils.DateUtils.isLunarMonthFirstDay(year, month, day);
                
                // 根据是否是农历月份第一天设置不同的显示格式
                String lunarDate;
                if (isLunarMonthFirstDay) {
                    // 农历月份第一天既显示月份又显示日
                    String monthName = com.example.mycalendarapp.utils.DateUtils.getLunarMonthName(year, month, day);
                    String dayName = com.example.mycalendarapp.utils.DateUtils.getLunarDayName(year, month, day);
                    lunarDate = monthName + dayName;
                } else {
                    // 其他日子只显示日
                    lunarDate = com.example.mycalendarapp.utils.DateUtils.getLunarDayName(year, month, day);
                }
                
                holder.lunarDayOfMonth.setText(lunarDate);
                
                // 检查是否是今天
                boolean isToday = position == todayPosition;
                // 检查是否是选中的日期
                boolean isSelected = position == selectedPosition;

                if (isSelected) {
                    // 选中日期使用深蓝色
                    holder.dayOfMonth.setTextColor(Color.WHITE);
                    holder.dayOfMonth.setBackgroundResource(R.drawable.selected_background);
                    holder.lunarDayOfMonth.setTextColor(Color.WHITE);
                } else if (isToday) {
                    // 今天使用浅蓝色（但不是选中状态）
                    holder.dayOfMonth.setTextColor(Color.BLUE);
                    holder.dayOfMonth.setBackgroundResource(R.drawable.today_background);
                    // 如果是今天且是农历月份第一天，使用特殊颜色
                    if (isLunarMonthFirstDay) {
                        holder.lunarDayOfMonth.setTextColor(Color.RED); // 农历月份第一天使用红色
                    } else {
                        holder.lunarDayOfMonth.setTextColor(Color.GRAY);
                    }
                } else {
                    // 其他日期普通显示
                    holder.dayOfMonth.setTextColor(Color.BLACK);
                    holder.dayOfMonth.setBackgroundResource(0);
                    // 如果是农历月份第一天，使用特殊颜色
                    if (isLunarMonthFirstDay) {
                        holder.lunarDayOfMonth.setTextColor(Color.RED); // 农历月份第一天使用红色
                    } else {
                        holder.lunarDayOfMonth.setTextColor(Color.GRAY);
                    }
                }
            } catch (NumberFormatException e) {
                holder.lunarDayOfMonth.setText("");
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
        public TextView lunarDayOfMonth;
        private OnItemListener onItemListener;

        public CalendarViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);
            dayOfMonth = itemView.findViewById(R.id.cellDayText);
            lunarDayOfMonth = itemView.findViewById(R.id.cellLunarDayText);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemListener.onItemClick(getAdapterPosition(), (String) dayOfMonth.getText());
        }
    }
}

