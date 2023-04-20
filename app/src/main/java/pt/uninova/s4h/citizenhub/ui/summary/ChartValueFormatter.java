package pt.uninova.s4h.citizenhub.ui.summary;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;

/** This class is used to format values that are added to the charts. */
public class ChartValueFormatter extends ValueFormatter {

    private final DecimalFormat mFormat = new DecimalFormat("###,###,###");

    /** Used to format a value.
     * @param value Value to be formatted.
     * @return String with the formatted value.
     */
    public String getFormattedValue(float value) {
         if(value <= -1){
            return "";
        }
        return mFormat.format(value);
    }

}
