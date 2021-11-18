package pt.uninova.s4h.citizenhub.persistence;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "measurement")
public class MedEx {

    @PrimaryKey(autoGenerate = true)
    private Integer id;
    @TypeConverters({EpochTypeConverter.class})
    private Date timestamp;
    @ColumnInfo(name = "kind_id")
    @TypeConverters(MeasurementKindTypeConverter.class)
    private MeasurementKind kind;
    private Integer repetitions;
    private Long trainingLength;
    private Double score;

    @Ignore
    public MedEx(Date timestamp, MeasurementKind kind,Integer repetitions, Long trainingLength, Double score) {
        this(null, timestamp, kind,repetitions,trainingLength, score);
    }

    public Integer getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(Integer repetitions) {
        this.repetitions = repetitions;
    }

    public Long getTrainingLength() {
        return trainingLength;
    }

    public void setTrainingLength(Long trainingLength) {
        this.trainingLength = trainingLength;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public MedEx(Integer id, Date timestamp, MeasurementKind kind, Integer repetitions, Long trainingLength, Double score) {
        this.id = id;
        this.timestamp = timestamp;
        this.kind = kind;
        this.repetitions = repetitions;
        this.trainingLength = trainingLength;
        this.score = score;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public MeasurementKind getKind() {
        return kind;
    }

    public void setKind(MeasurementKind kind) {
        this.kind = kind;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}