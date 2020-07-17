package pt.uninova.s4h.citizenhub.datastorage;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Device.class, Source.class, Measurement.class}, views = {AvgMeasurementView.class}, version = 2)
@TypeConverters({Converters.class})

public abstract class CitizenDatabase extends RoomDatabase {

    public abstract DeviceDAO deviceDao();

    public abstract SourceDAO sourceDAO();

    public abstract MeasurementDAO measurementDAO();

}
