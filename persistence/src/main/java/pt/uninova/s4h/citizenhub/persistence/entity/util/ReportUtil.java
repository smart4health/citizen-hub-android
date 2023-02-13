package pt.uninova.s4h.citizenhub.persistence.entity.util;

import androidx.room.TypeConverters;

import java.time.Duration;
import java.time.Instant;

import pt.uninova.s4h.citizenhub.persistence.conversion.DurationTypeConverter;

public class ReportUtil {

    //Breathing
    private Double maxBreathingRate;
    private Double minBreathingRate;
    private Double avgBreathingRate;

    //Calories
    private Double calories;

    //Distance
    private Double distance;

    //Heart Rate
    private Double maxHeartRate;
    private Double minHeartRate;
    private Double avgHeartRate;

    //Position
    private Integer postureClassification;
    @TypeConverters(DurationTypeConverter.class)
    private Duration postureDuration;
    @TypeConverters(DurationTypeConverter.class)
    private Duration correctPostureDuration;
    @TypeConverters(DurationTypeConverter.class)
    private Duration wrongPostureDuration;

    //Steps
    private Integer steps;

    //Timestamp
    private Instant timestamp;

    private Long id;

    //Breathing Rate
    public Double getMaxBreathingRate(){ return maxBreathingRate; }

    public void setMaxBreathingRate(Double maxBreathingRate) { this.maxBreathingRate = maxBreathingRate; }

    public Double getMinBreathingRate(){ return minBreathingRate; }

    public void setMinBreathingRate(Double minBreathingRate) { this.minBreathingRate = minBreathingRate; }

    public Double getAvgBreathingRate(){ return avgBreathingRate; }

    public void setAvgBreathingRate(Double avgBreathingRate) { this.avgBreathingRate = avgBreathingRate; }

    //Calories
    public Double getCalories(){ return calories; }

    public void setCalories(Double calories){ this.calories = calories;}

    //Distance
    public Double getDistance(){return distance;}

    public void setDistance(Double distance){this.distance = distance;}

    //Heart Rate
    public Double getMaxHeartRate(){
        return maxHeartRate;
    }

    public void setMaxHeartRate(Double maxHeartRate) { this.maxHeartRate = maxHeartRate; }

    public Double getMinHeartRate(){
        return minHeartRate;
    }

    public void setMinHeartRate(Double minHeartRate) { this.minHeartRate = minHeartRate; }

    public Double getAvgHeartRate(){
        return avgHeartRate;
    }

    public void setAvgHeartRate(Double avgHeartRate) { this.avgHeartRate = avgHeartRate; }

    //Posture
    public Integer getPostureClassification(){ return postureClassification; }

    public void setPostureClassification(Integer postureClassification){ this.postureClassification = postureClassification; }

    public Duration getPostureDuration(){ return postureDuration; }

    public void setPostureDuration(Duration postureDuration){ this.postureDuration = postureDuration; }

    public Duration getCorrectPostureDuration(){ return correctPostureDuration; }

    public void setCorrectPostureDuration(Duration correctPostureDuration){ this.correctPostureDuration = correctPostureDuration; }

    public Duration getWrongPostureDuration(){ return wrongPostureDuration; }

    public void setWrongPostureDuration(Duration wrongPostureDuration){ this.wrongPostureDuration = wrongPostureDuration; }

    //Steps
    public Integer getSteps(){return steps;}

    public void setSteps(Integer steps) {this.steps = steps;}

    //Timestamp
    public Instant getTimestamp(){return timestamp;}

    public void setTimestamp(Instant timestamp){this.timestamp=timestamp;}

    //ID
    public Long getId(){return id;}

    public void setId(Long id){this.id=id;}
}
