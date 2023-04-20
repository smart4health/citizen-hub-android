package pt.uninova.s4h.citizenhub.persistence.entity.util;

import androidx.room.TypeConverters;

import java.time.Duration;
import java.time.Instant;

import pt.uninova.s4h.citizenhub.persistence.conversion.DurationTypeConverter;

/** Sample with all Lumbar Extension Training attributes. */
public class LumbarExtensionTrainingSample {

    @TypeConverters(DurationTypeConverter.class)
    private Duration duration;
    private Double score;
    private Integer repetitions;
    private Integer weight;
    private Double calories;

    //Timestamp
    private Instant timestamp;

    public LumbarExtensionTrainingSample(Duration duration, Integer repetitions, Integer weight, Double calories, Instant timestamp) {
        this.duration = duration;
        this.repetitions = repetitions;
        this.weight = weight;
        this.calories = calories;
        this.timestamp = timestamp;
    }

    public Double getCalories() {
        return calories;
    }

    public Duration getDuration() {
        return duration;
    }

    public Integer getRepetitions() {
        return repetitions;
    }

    public Double getScore() {
        return score;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setRepetitions(Integer repetitions) {
        this.repetitions = repetitions;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    //Timestamp
    public Instant getTimestamp(){return timestamp;}

    public void setTimestamp(Instant timestamp){this.timestamp=timestamp;}

}
