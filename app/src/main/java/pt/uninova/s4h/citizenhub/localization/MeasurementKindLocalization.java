package pt.uninova.s4h.citizenhub.localization;

import android.content.Context;
import android.content.res.Resources;

import java.util.Locale;

public class MeasurementKindLocalization {

    private final Context context;

    public MeasurementKindLocalization(Context context) {
        this.context = context;
    }

    public String localize(int measurementKind) {
        final Resources resources = context.getResources();

        final int resourceId = resources.getIdentifier(String.format(Locale.getDefault(), "measurement_type.%d", measurementKind), "string", context.getPackageName());

        return resourceId != 0 ? resources.getString(resourceId) : "";
    }
}
