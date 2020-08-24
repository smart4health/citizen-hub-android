package pt.uninova.s4h.citizenhub.persistence;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "measurement")
public class Measurement {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @TypeConverters({TimestampConverter.class})
    private Date timestamp;
    private String value;
    @TypeConverters(CharacteristicTypeConverter.class)
    private int type;

    @Ignore
    public Measurement() {

    }

    public Measurement(Integer id, Date timestamp, String value, int type) {
        this.id = id;
        // this.uuid = uuid;
        this.timestamp = timestamp;
        this.value = value;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}