package pt.uninova.s4h.citizenhub;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;


public class SettingsFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String KEY_WORK_DAYS = "workDays";
    private static final String KEY_WORK_TIME_START = "workStart";
    private static final String KEY_WORK_TIME_END = "workEnd";
    private LinearLayout workDaysLayout;
    private TextView workDaysPlaceholder;
    private TextView startTimePlaceHolder;
    private TextView endtimePlaceHolder;
    private int timePickerTheme;
    private SharedPreferences preferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        workDaysLayout = getView().findViewById(R.id.layout_work_days);
        workDaysPlaceholder = getView().findViewById(R.id.placeholder_workdays);

        Set<String> workDaysSet = new HashSet<>();
        workDaysSet.add("");
        workDaysSet = preferences.getStringSet(KEY_WORK_DAYS, workDaysSet);
        if (!workDaysSet.contains("")) {
            workDaysPlaceholder.setText(workDaysSet.toString().replaceAll("[\\[\\]]", ""));
        }

        LinearLayout startTime = getView().findViewById(R.id.layout_start_time);
        startTimePlaceHolder = getView().findViewById(R.id.placeholder_work_start_time);

        startTimePlaceHolder.setText(preferences.getString(KEY_WORK_TIME_START, getResources().getString(R.string.fragment_settings_work_hours_start_message)));

        LinearLayout endTime = getView().findViewById(R.id.layout_end_time);
        endtimePlaceHolder = getView().findViewById(R.id.placeholder_work_end_time);
        endtimePlaceHolder.setText(preferences.getString(KEY_WORK_TIME_END, getResources().getString(R.string.fragment_settings_work_hours_start_message)));


        Set<String> finalWorkDaysSet = workDaysSet;
        Set<String> workDaysTest = new HashSet<>();
        workDaysLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogThemeWithCheckboxes);
                builder.setTitle(getResources().getString(R.string.fragment_settings_work_days_title));

                String[] workDays = getResources().getStringArray(R.array.workdays);
                String finalWorkDays = String.valueOf(preferences.getStringSet(KEY_WORK_DAYS, finalWorkDaysSet));
                Set<String> workDaysInteger = new HashSet<>();
                workDaysInteger = preferences.getStringSet(KEY_WORK_DAYS, workDaysInteger);


                int j = 0;
                boolean[] checkedItems = new boolean[7];

                for (int k = 0; k < checkedItems.length; k++) {

                    checkedItems[k] = workDaysInteger.contains(String.valueOf(k));
                    System.out.println(checkedItems.length);
                    System.out.println(k);
                    System.out.println(workDaysInteger);
                    System.out.println(Arrays.toString(checkedItems) + " CHECKED ITEMS");
                }

//                for (String day : workDays
//                ) {
//                    checkedItems[j] = finalWorkDays.contains(day);
//                    j++;
//                }
                builder.setMultiChoiceItems(workDays, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        workDaysPlaceholder.setText("");
                        StringJoiner sb = new StringJoiner(", ");
                        for (int i = 0; i < workDays.length; i++) {
                            if (checkedItems[i]) {
                                sb.add(workDays[i]);
                                workDaysTest.add(String.valueOf(i));
                            }
                            else{
                                workDaysTest.remove(String.valueOf(i));
                            }
                        }
                        workDaysPlaceholder.setText(sb.toString());
//                        preferences.edit().putStringSet(KEY_WORK_DAYS, Collections.singleton(sb.toString())).apply();
                        preferences.edit().putStringSet(KEY_WORK_DAYS, workDaysTest).apply();

                    }
                });

                builder.setPositiveButton(getResources().getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        workDaysPlaceholder.setText("");
                        StringJoiner sb = new StringJoiner(", ");
                        for (int i = 0; i < workDays.length; i++) {
                            if (checkedItems[i]) {
                                sb.add(workDays[i]);
                            }
                        }
                        workDaysPlaceholder.setText(sb.toString());
//                        preferences.edit().putStringSet(KEY_WORK_DAYS, Collections.singleton(sb.toString())).apply();
                        preferences.edit().putStringSet(KEY_WORK_DAYS, workDaysTest).apply();

                    }

                });
                builder.setNegativeButton(getResources().getString(R.string.label_cancel), null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get Current Time
                final Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), R.style.TimePickerTheme,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                startTimePlaceHolder.setText(String.format("%02d:%02d", hourOfDay, minute));
                                preferences.edit().putString(KEY_WORK_TIME_START, String.format("%02d:%02d", hourOfDay, minute)).apply();
                            }
                        }, mHour, mMinute, true);
                timePickerDialog.setTitle(getResources().getString(R.string.fragment_settings_work_hours_start_message));
                timePickerDialog.show();
            }

        });
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int mHours = c.get(Calendar.HOUR_OF_DAY);
                int mMinutes = c.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), R.style.TimePickerTheme,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                endtimePlaceHolder.setText(String.format("%02d:%02d", hourOfDay, minute));
                                preferences.edit().putString(KEY_WORK_TIME_END, String.format("%02d:%02d", hourOfDay, minute)).apply();

                            }
                        }, mHours, mMinutes, true);
                timePickerDialog.setTitle(getResources().getString(R.string.fragment_settings_work_hours_end_message));
                timePickerDialog.show();
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }
}
