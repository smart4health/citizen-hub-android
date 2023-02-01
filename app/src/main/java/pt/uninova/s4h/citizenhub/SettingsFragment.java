package pt.uninova.s4h.citizenhub;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String KEY_WORK_DAYS = "workDays";
    private static final String KEY_WORK_TIME_START = "workStart";
    private static final String KEY_WORK_TIME_END = "workEnd";

    private SharedPreferences preferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext());
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager.getDefaultSharedPreferences(requireContext());
//        setPreferencesFromResource(R.xml.settings_fragment, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof TimePreference) {
            dialogFragment = new TimePreferenceDialogFragmentCompat();
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);

        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getParentFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.accounts_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updatePreference(KEY_WORK_DAYS);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(key);
    }

    private void updatePreference(String key) {
        if (key.equals(KEY_WORK_DAYS)) {
            try {
                Preference preference = findPreference(key);

                if (preference != null) {
                    List<DayOfWeek> daysOfWeek = getDaysOfWeek((MultiSelectListPreference) preference);

                    if (daysOfWeek.size() > 0) {
                        Set<String> values = new HashSet<>();
                        final StringBuilder dayString = new StringBuilder();

                        for (DayOfWeek i : daysOfWeek) {
                            dayString.append(" ");
                            dayString.append(i.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()));
                            values.add(String.valueOf(i.getValue()));
                        }

                        preferences.edit().putStringSet(KEY_WORK_DAYS, values).apply();
                        preference.setSummary(getString(R.string.fragment_settings_current_work_days_text) + dayString);
                    } else {
                        preferences.edit().remove(KEY_WORK_DAYS).apply();
                        preference.setSummary(getString(R.string.fragment_settings_choose_work_days_text));
                    }
                }
            } catch (Exception e) {
                final SharedPreferences.Editor editor = preferences.edit();

                editor.remove(KEY_WORK_DAYS);
                editor.remove(KEY_WORK_TIME_START);
                editor.remove(KEY_WORK_TIME_END);

                editor.apply();

                Preference preference = findPreference(KEY_WORK_DAYS);
                preference.setSummary(getString(R.string.fragment_settings_choose_work_days_text));
            }
        }


        WorkTimeRangeConverter workTimeRangeConverter = WorkTimeRangeConverter.getInstance(requireContext());
        workTimeRangeConverter.refreshTimeVariables(requireContext());
    }


    public List<DayOfWeek> getDaysOfWeek(MultiSelectListPreference preference) {
        List<DayOfWeek> dayOfWeekList = new LinkedList<>();

        for (String i : preference.getValues()) {
            dayOfWeekList.add(DayOfWeek.of(Integer.parseInt(i)));
        }

        return dayOfWeekList;
    }
}
