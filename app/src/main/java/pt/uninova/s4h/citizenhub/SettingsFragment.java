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
import java.util.Collections;
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

//    @Override
//    public void onDisplayPreferenceDialog(Preference preference) {
//        DialogFragment dialogFragment = null;
//        if (preference instanceof TimePreference) {
//            dialogFragment = new TimePreferenceDialogFragmentCompat();
//            Bundle bundle = new Bundle(1);
//            bundle.putString("key", preference.getKey());
//            dialogFragment.setArguments(bundle);
//
//        }
//
//        if (dialogFragment != null) {
//            dialogFragment.setTargetFragment(this, 0);
//            dialogFragment.show(this.getParentFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
//        } else {
//            super.onDisplayPreferenceDialog(preference);
//        }
//    }

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
        //                preference.setSummary(getString(R.string.fragment_settings_choose_work_days_text));
//
        LinearLayout startTime = getView().findViewById(R.id.layout_start_time);
        startTimePlaceHolder = getView().findViewById(R.id.placeholder_work_start_time);

        startTimePlaceHolder.setText(preferences.getString(KEY_WORK_TIME_START, "Set some time"));

        LinearLayout endTime = getView().findViewById(R.id.layout_end_time);
        endtimePlaceHolder = getView().findViewById(R.id.placeholder_work_end_time);
        endtimePlaceHolder.setText(preferences.getString(KEY_WORK_TIME_END, "Set some time"));


        Set<String> finalWorkDaysSet = workDaysSet;
        workDaysLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set up the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogThemeWithCheckboxes);
                builder.setTitle("Choose work days");

// Add a checkbox list
                String[] workDays = getResources().getStringArray(R.array.workdays);
                String finalWorkDays = String.valueOf(preferences.getStringSet(KEY_WORK_DAYS, finalWorkDaysSet));
                int j = 0;
                boolean[] checkedItems = new boolean[7];
                for (String day : workDays
                ) {
                    System.out.println(day);
                    System.out.println(finalWorkDays);
                    System.out.println(Arrays.toString(workDays));
                    checkedItems[j] = finalWorkDays.contains(day);
                    System.out.println(Arrays.toString(checkedItems));
                    j++;
                }
                builder.setMultiChoiceItems(workDays, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        workDaysPlaceholder.setText("");
                        StringJoiner sb = new StringJoiner(", ");
                        for (int i = 0; i < workDays.length; i++) {
                            if (checkedItems[i]) {
                                sb.add(workDays[i]);
                            }
                        }
                        workDaysPlaceholder.setText(sb.toString());
                        preferences.edit().putStringSet(KEY_WORK_DAYS, Collections.singleton(sb.toString())).apply();

                    }
                });

// Add OK and Cancel buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
                        preferences.edit().putStringSet(KEY_WORK_DAYS, Collections.singleton(sb.toString())).apply();
                    }

                });
                builder.setNegativeButton("Cancel", null);
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
                timePickerDialog.setTitle("Choose end hour:");
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
                timePickerDialog.setTitle("Choose end time:");
                timePickerDialog.show();
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

//    private void updatePreference(String key) {
//        if (key.equals(KEY_WORK_DAYS)) {
//            try {
//                Preference preference = findPreference(key);
//
//                if (preference != null) {
//                    List<DayOfWeek> daysOfWeek = getDaysOfWeek((MultiSelectListPreference) preference);
//
//                    if (daysOfWeek.size() > 0) {
//                        Set<String> values = new HashSet<>();
//                        final StringBuilder dayString = new StringBuilder();
//
//                        for (DayOfWeek i : daysOfWeek) {
//                            dayString.append(" ");
//                            dayString.append(i.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()));
//                            values.add(String.valueOf(i.getValue()));
//                        }
//
//                        preferences.edit().putStringSet(KEY_WORK_DAYS, values).apply();
//                        preference.setSummary(getString(R.string.fragment_settings_current_work_days_text) + dayString);
//                    } else {
//                        preferences.edit().remove(KEY_WORK_DAYS).apply();
//                        preference.setSummary(getString(R.string.fragment_settings_choose_work_days_text));
//                    }
//                }
//            } catch (Exception e) {
//                final SharedPreferences.Editor editor = preferences.edit();
//
//                editor.remove(KEY_WORK_DAYS);
//                editor.remove(KEY_WORK_TIME_START);
//                editor.remove(KEY_WORK_TIME_END);
//
//                editor.apply();
//
//                Preference preference = findPreference(KEY_WORK_DAYS);
//                preference.setSummary(getString(R.string.fragment_settings_choose_work_days_text));
//            }
//        }
//
//
//        WorkTimeRangeConverter workTimeRangeConverter = WorkTimeRangeConverter.getInstance(requireContext());
//        workTimeRangeConverter.refreshTimeVariables(requireContext());
//    }
}
