package com.example.recipe_android_project.features.plan.presentation.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_android_project.R;
import com.example.recipe_android_project.features.plan.domain.model.DayModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WeekPagerAdapter extends RecyclerView.Adapter<WeekPagerAdapter.WeekViewHolder> {

    private static final int TOTAL_WEEKS = 10000;
    public static final int CENTER_POSITION = TOTAL_WEEKS / 2;

    private final Calendar baseCalendar;
    private final String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private final int todayDay, todayMonth, todayYear;

    private OnDaySelectedListener listener;
    private int selectedDay, selectedMonth, selectedYear;

    private int currentVisiblePosition = CENTER_POSITION;

    private final int[] containerIds = {
            R.id.dayContainer0, R.id.dayContainer1, R.id.dayContainer2,
            R.id.dayContainer3, R.id.dayContainer4, R.id.dayContainer5, R.id.dayContainer6
    };
    private final int[] dayNameIds = {
            R.id.tvDayName0, R.id.tvDayName1, R.id.tvDayName2,
            R.id.tvDayName3, R.id.tvDayName4, R.id.tvDayName5, R.id.tvDayName6
    };
    private final int[] dayNumberIds = {
            R.id.tvDayNumber0, R.id.tvDayNumber1, R.id.tvDayNumber2,
            R.id.tvDayNumber3, R.id.tvDayNumber4, R.id.tvDayNumber5, R.id.tvDayNumber6
    };

    public interface OnDaySelectedListener {
        void onDaySelected(DayModel day);
        void onWeekChanged(int month, int year, int dominantMonth, int dominantYear);
    }

    public WeekPagerAdapter(OnDaySelectedListener listener) {
        this.listener = listener;

        Calendar today = Calendar.getInstance();
        todayDay = today.get(Calendar.DAY_OF_MONTH);
        todayMonth = today.get(Calendar.MONTH);
        todayYear = today.get(Calendar.YEAR);

        selectedDay = todayDay;
        selectedMonth = todayMonth;
        selectedYear = todayYear;

        baseCalendar = Calendar.getInstance();
        setToStartOfWeek(baseCalendar);
    }

    private void setToStartOfWeek(Calendar cal) {
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    public int getPositionForDate(int day, int month, int year) {
        Calendar targetDate = Calendar.getInstance();
        targetDate.set(Calendar.YEAR, year);
        targetDate.set(Calendar.MONTH, month);
        targetDate.set(Calendar.DAY_OF_MONTH, day);
        setToStartOfWeek(targetDate);

        long diffInMillis = targetDate.getTimeInMillis() - baseCalendar.getTimeInMillis();
        long diffInWeeks = diffInMillis / (1000L * 60 * 60 * 24 * 7);

        return CENTER_POSITION + (int) diffInWeeks;
    }

    public void setSelectedDate(int day, int month, int year) {
        int oldPosition = getPositionForDate(selectedDay, selectedMonth, selectedYear);

        selectedDay = day;
        selectedMonth = month;
        selectedYear = year;

        int newPosition = getPositionForDate(day, month, year);

        notifyItemChanged(oldPosition);
        if (newPosition != oldPosition) {
            notifyItemChanged(newPosition);
        }
    }

    @NonNull
    @Override
    public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_week, parent, false);
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekViewHolder holder, int position) {
        List<DayModel> days = generateWeekDays(position);

        for (int i = 0; i < 7; i++) {
            DayModel day = days.get(i);

            LinearLayout container = holder.itemView.findViewById(containerIds[i]);
            TextView tvDayName = holder.itemView.findViewById(dayNameIds[i]);
            TextView tvDayNumber = holder.itemView.findViewById(dayNumberIds[i]);

            tvDayName.setText(day.getDayName());
            tvDayNumber.setText(String.valueOf(day.getDayNumber()));

            applyDayStyle(container, tvDayName, tvDayNumber, day);

            final int dayIndex = i;
            container.setOnClickListener(v -> {
                DayModel clickedDay = days.get(dayIndex);

                int oldPosition = getPositionForDate(selectedDay, selectedMonth, selectedYear);

                selectedDay = clickedDay.getDayNumber();
                selectedMonth = clickedDay.getMonth();
                selectedYear = clickedDay.getYear();

                notifyItemChanged(oldPosition);
                notifyItemChanged(holder.getAdapterPosition());

                if (listener != null) {
                    listener.onDaySelected(clickedDay);
                }
            });
        }
    }

    private void applyDayStyle(LinearLayout container, TextView tvDayName,
                               TextView tvDayNumber, DayModel day) {
        if (day.isSelected()) {
            container.setBackgroundResource(R.drawable.day_background_selected);
            tvDayName.setTextColor(Color.WHITE);
            tvDayNumber.setTextColor(Color.WHITE);
        } else if (day.isToday()) {
            container.setBackgroundResource(R.drawable.day_background_today);
            tvDayName.setTextColor(Color.parseColor("#E27036"));
            tvDayNumber.setTextColor(Color.parseColor("#E27036"));
        } else {
            container.setBackgroundResource(R.drawable.day_background_normal);
            tvDayName.setTextColor(Color.parseColor("#6B6B6B"));
            tvDayNumber.setTextColor(Color.parseColor("#2D2D2D"));
        }
    }
    private List<DayModel> generateWeekDays(int position) {
        List<DayModel> days = new ArrayList<>();

        Calendar weekStart = (Calendar) baseCalendar.clone();
        int weekOffset = position - CENTER_POSITION;
        weekStart.add(Calendar.WEEK_OF_YEAR, weekOffset);

        for (int i = 0; i < 7; i++) {
            int dayOfWeek = weekStart.get(Calendar.DAY_OF_WEEK);
            int dayOfMonth = weekStart.get(Calendar.DAY_OF_MONTH);
            int month = weekStart.get(Calendar.MONTH);
            int year = weekStart.get(Calendar.YEAR);

            String dayName = dayNames[dayOfWeek - 1];

            boolean isToday = (dayOfMonth == todayDay && month == todayMonth && year == todayYear);
            boolean isSelected = (dayOfMonth == selectedDay && month == selectedMonth && year == selectedYear);

            days.add(new DayModel(dayName, dayOfMonth, month, year, isSelected, isToday));

            weekStart.add(Calendar.DAY_OF_MONTH, 1);
        }

        return days;
    }

    public int[] getDominantMonthYear(int position) {
        List<DayModel> days = generateWeekDays(position);

        int[] monthCount = new int[12];
        int[][] monthYearPairs = new int[7][2];

        for (int i = 0; i < days.size(); i++) {
            DayModel day = days.get(i);
            monthYearPairs[i][0] = day.getMonth();
            monthYearPairs[i][1] = day.getYear();
            monthCount[day.getMonth()]++;
        }

        int maxCount = 0;
        int dominantMonth = days.get(0).getMonth();
        int dominantYear = days.get(0).getYear();

        for (int i = 0; i < 7; i++) {
            int month = monthYearPairs[i][0];
            if (monthCount[month] > maxCount) {
                maxCount = monthCount[month];
                dominantMonth = month;
                dominantYear = monthYearPairs[i][1];
            }
        }

        return new int[]{dominantMonth, dominantYear};
    }
    public int[] getFirstDayMonthYear(int position) {
        Calendar weekStart = (Calendar) baseCalendar.clone();
        int weekOffset = position - CENTER_POSITION;
        weekStart.add(Calendar.WEEK_OF_YEAR, weekOffset);

        return new int[]{weekStart.get(Calendar.MONTH), weekStart.get(Calendar.YEAR)};
    }
    public void notifyWeekChanged(int position) {
        currentVisiblePosition = position;

        int[] firstDay = getFirstDayMonthYear(position);
        int[] dominant = getDominantMonthYear(position);

        if (listener != null) {
            listener.onWeekChanged(firstDay[0], firstDay[1], dominant[0], dominant[1]);
        }
    }
    @Override
    public int getItemCount() {
        return TOTAL_WEEKS;
    }
    static class WeekViewHolder extends RecyclerView.ViewHolder {
        public WeekViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
