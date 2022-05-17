package pt.uninova.s4h.citizenhub.persistence.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.time.Instant;

import pt.uninova.s4h.citizenhub.persistence.conversion.EpochTypeConverter;

@Entity(
        tableName = "sample",
        foreignKeys = {
                @ForeignKey(entity = DeviceRecord.class,
                        parentColumns = "id",
                        childColumns = "device_id",
                        onDelete = ForeignKey.SET_NULL)
        },
        indices = @Index("device_id")
)
public class SampleRecord {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "device_id")
    private Long deviceId;

    @TypeConverters(EpochTypeConverter.class)
    private Instant timestamp;

    public SampleRecord(Long id, Long deviceId, Instant timestamp) {
        this.id = id;
        this.deviceId = deviceId;
        this.timestamp = timestamp;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public Long getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
