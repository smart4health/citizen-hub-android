package pt.uninova.s4h.citizenhub.datastorage;

import androidx.room.DatabaseView;

import java.util.Date;


@DatabaseView("SELECT SUM(value) AS AverageValue , date(timestamp) AS Date, name as CharacteristicName FROM measurements GROUP BY Date, CharacteristicName")
public class SumMeasurement {
    public float SumValue;
    public Date date;
    public String characteristicName;
}

