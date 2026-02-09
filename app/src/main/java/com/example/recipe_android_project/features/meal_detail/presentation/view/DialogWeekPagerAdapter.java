package com.example.recipe_android_project.features.meal_detail.presentation.view;

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

public class DialogWeekPagerAdapter extends RecyclerView.Adapter<DialogWeekPagerAdapter.WeekViewHolder> {

    private static final int TOTAL_WEEKS = 5000;
    public static final int CENTER_POSITION = 0;

    private final Calendar baseCalendar;
    private final String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private final int todayDay, todayMonth, todayYear;
    private final long todayTimeInMillis;

    private OnDaySelectedListener listener;
    private int selectedDay, selectedMonth, selectedYear;

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

    public DialogWeekPagerAdapter(OnDaySelectedListener listener) {
        this.listener = listener;

        Calendar today = Calendar.getInstance();
        todayDay = today.get(Calendar.DAY_OF_MONTH);
        todayMonth = today.get(Calendar.MONTH);
        todayYear = today.get(Calendar.YEAR);

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        todayTimeInMillis = today.getTimeInMillis();

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

    private boolean isPastDate(int day, int month, int year) {
        Calendar checkDate = Calendar.getInstance();
        checkDate.set(Calendar.YEAR, year);
        checkDate.set(Calendar.MONTH, month);
        checkDate.set(Calendar.DAY_OF_MONTH, day);
        checkDate.set(Calendar.HOUR_OF_DAY, 0);
        checkDate.set(Calendar.MINUTE, 0);
        checkDate.set(Calendar.SECOND, 0);
        checkDate.set(Calendar.MILLISECOND, 0);

        return checkDate.getTimeInMillis() < todayTimeInMillis;
    }

    public int getPositionForDate(int day, int month, int year) {
        if (isPastDate(day, month, year)) {
            return CENTER_POSITION;
        }

        Calendar targetDate = Calendar.getInstance();
        targetDate.set(Calendar.YEAR, year);
        targetDate.set(Calendar.MONTH, month);
        targetDate.set(Calendar.DAY_OF_MONTH, day);
        setToStartOfWeek(targetDate);

        long diffInMillis = targetDate.getTimeInMillis() - baseCalendar.getTimeInMillis();
        long diffInWeeks = diffInMillis / (1000L * 60 * 60 * 24 * 7);

        int position = CENTER_POSITION + (int) diffInWeeks;
        return Math.max(CENTER_POSITION, position);
    }

    public void setSelectedDate(int day, int month, int year) {
        if (isPastDate(day, month, year)) {
            return;
        }

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

    public void notifyWeekChanged(int position) {
        int[] dominant = getDominantMonthYear(position);
        int[] firstDay = getFirstDayMonthYear(position);

        if (listener != null) {
            listener.onWeekChanged(firstDay[0], firstDay[1], dominant[0], dominant[1]);
        }
    }

    public int[] getDominantMonthYear(int position) {
        List<DayModel> days = generateWeekDays(position);

        int[] monthCount = new int[12];
        int[][] monthYearPairs = new int[7][2];

        for (int i = 0; i < days.size(); i++) {
            DayModel day = days.get(i);
            if (!day.isPast()) {
                monthYearPairs[i][0] = day.getMonth();
                monthYearPairs[i][1] = day.getYear();
                monthCount[day.getMonth()]++;
            }
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

        if (dominantYear < todayYear || (dominantYear == todayYear && dominantMonth < todayMonth)) {
            dominantMonth = todayMonth;
            dominantYear = todayYear;
        }

        return new int[]{dominantMonth, dominantYear};
    }

    public int[] getFirstDayMonthYear(int position) {
        Calendar weekStart = (Calendar) baseCalendar.clone();
        int weekOffset = position - CENTER_POSITION;
        weekStart.add(Calendar.WEEK_OF_YEAR, weekOffset);

        int month = weekStart.get(Calendar.MONTH);
        int year = weekStart.get(Calendar.YEAR);

        if (year < todayYear || (year == todayYear && month < todayMonth)) {
            month = todayMonth;
            year = todayYear;
        }

        return new int[]{month, year};
    }

    public int getMinPosition() {
        return CENTER_POSITION;
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

            if (container == null || tvDayName == null || tvDayNumber == null) {
                continue;
            }

            tvDayName.setText(day.getDayName());
            tvDayNumber.setText(String.valueOf(day.getDayNumber()));

            applyDayStyle(container, tvDayName, tvDayNumber, day);

            final int dayIndex = i;

            if (!day.isPast()) {
                container.setEnabled(true);
                container.setClickable(true);
                container.setOnClickListener(v -> {
                    DayModel clickedDay = days.get(dayIndex);

                    if (clickedDay.isPast()) {
                        return;
                    }

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
            } else {
                container.setEnabled(false);
                container.setClickable(false);
                container.setOnClickListener(null);
            }
        }
    }

    private void applyDayStyle(LinearLayout container, TextView tvDayName,
                               TextView tvDayNumber, DayModel day) {
        if (day.isPast()) {
            container.setBackgroundResource(R.drawable.day_background_disabled);
            tvDayName.setTextColor(Color.parseColor("#CCCCCC"));
            tvDayNumber.setTextColor(Color.parseColor("#CCCCCC"));
            container.setAlpha(0.5f);
        } else if (day.isSelected()) {
            container.setBackgroundResource(R.drawable.day_background_selected);
            tvDayName.setTextColor(Color.WHITE);
            tvDayNumber.setTextColor(Color.WHITE);
            container.setAlpha(1f);
        } else if (day.isToday()) {
            container.setBackgroundResource(R.drawable.day_background_today);
            tvDayName.setTextColor(Color.parseColor("#E27036"));
            tvDayNumber.setTextColor(Color.parseColor("#E27036"));
            container.setAlpha(1f);
        } else {
            container.setBackgroundResource(R.drawable.day_background_normal);
            tvDayName.setTextColor(Color.parseColor("#6B6B6B"));
            tvDayNumber.setTextColor(Color.parseColor("#2D2D2D"));
            container.setAlpha(1f);
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
            boolean isPast = isPastDate(dayOfMonth, month, year);

            DayModel dayModel = new DayModel(dayName, dayOfMonth, month, year, isSelected, isToday, isPast);
            days.add(dayModel);

            weekStart.add(Calendar.DAY_OF_MONTH, 1);
        }

        return days;
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
