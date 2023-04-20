package pt.uninova.s4h.citizenhub;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class WorkTimeRangeConverter {

    private static final String KEY_WORK_DAYS = "workDays";
    private static final String KEY_WORK_TIME_START = "workStart";
    private static final String KEY_WORK_TIME_END = "workEnd";
    private static volatile WorkTimeRangeConverter instance = null;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    private LocalTime workStart;
    private LocalTime workEnd;
    private Set<DayOfWeek> workDays;

    private WorkTimeRangeConverter(Context context) {
        workDays = new HashSet<>();
        load(context);
    }

    public static WorkTimeRangeConverter getInstance(Context context) {
        if (instance == null) {
            synchronized (WorkTimeRangeConverter.class) {
                if (instance == null) {
                    instance = new WorkTimeRangeConverter(context);
                }
            }
        }

        return instance;
    }

    private LocalTime stringToLocalTime(String timeString) {
        return LocalTime.parse(timeString, formatter);
    }

    public void load(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        workStart = stringToLocalTime(preferences.getString(KEY_WORK_TIME_START, "09:00"));
        workEnd = stringToLocalTime(preferences.getString(KEY_WORK_TIME_END, "17:00"));
        workDays.clear();

        for (String i : preferences.getStringSet(KEY_WORK_DAYS, new HashSet<>())) {
            workDays.add(DayOfWeek.of(Integer.parseInt(i)));
        }
    }

    public boolean isWorkTime(LocalDateTime localDateTime) {
        return workDays.contains(localDateTime.getDayOfWeek()) && localDateTime.toLocalTime().isAfter(workStart) && localDateTime.toLocalTime().isBefore(workEnd);
    }

    public void refreshTimeVariables(Context context) {
        load(context);
    }

    public LocalTime getWorkStart() {
        return workStart;
    }

    public void setWorkStart(LocalTime workStart) {
        this.workStart = workStart;
    }

    public LocalTime getWorkEnd() {
        return workEnd;
    }

    public void setWorkEnd(LocalTime workEnd) {
        this.workEnd = workEnd;
    }

}