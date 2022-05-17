package pt.uninova.s4h.citizenhub.persistence.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "calories_snapshot_measurement",
        foreignKeys = {
                @ForeignKey(
                        entity = SampleRecord.class,
                        parentColumns = "id",
                        childColumns = "sample_id")
        },
        indices = @Index("sample_id")
)
public class CaloriesSnapshotMeasurementRecord {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "sample_id")
    private Long sampleId;

    private Integer type;
    private Double value;

    public CaloriesSnapshotMeasurementRecord(Long id, Long sampleId, Integer type, Double value) {
        this.id = id;
        this.sampleId = sampleId;
        this.type = type;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public Long getSampleId() {
        return sampleId;
    }

    public Integer getType() {
        return type;
    }

    public Double getValue() {
        return value;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
