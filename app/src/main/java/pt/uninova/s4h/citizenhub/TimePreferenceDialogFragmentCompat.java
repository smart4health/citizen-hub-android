package pt.uninova.s4h.citizenhub;

import android.content.Context;
import android.view.View;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;

public class TimePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat implements DialogPreference.TargetFragment
{
    TimePicker timePicker = null;

    @Override
    protected View onCreateDialogView(Context context)
    {
        timePicker = new TimePicker(context);
        return (timePicker);
    }

    @Override
    protected void onBindDialogView(View v)
    {
        super.onBindDialogView(v);
        timePicker.setIs24HourView(true);
        TimePreference pref = (TimePreference) getPreference();
        timePicker.setHour(pref.hour);
        timePicker.setMinute(pref.minute);
    }


    @Override
    public void onDialogClosed(boolean positiveResult)
    {
        if (positiveResult)
        {
            TimePreference pref = (TimePreference) getPreference();
            pref.hour = timePicker.getHour();
            pref.minute = timePicker.getMinute();

            String value = TimePreference.timeToString(pref.hour, pref.minute);
            if (pref.callChangeListener(value)) pref.persistStringValue(value);
            pref.setSummary(pref.getSummary());
        }

    }

    @Override
    public Preference findPreference(@NonNull CharSequence charSequence)
    {
        return getPreference();
    }
}
