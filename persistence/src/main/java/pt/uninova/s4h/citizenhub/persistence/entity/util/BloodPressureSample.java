package pt.uninova.s4h.citizenhub.persistence.entity.util;

import java.time.Instant;

/** Sample with all Blood Pressure attributes. */
public class BloodPressureSample {

    private Double systolic;
    private Double diastolic;
    private Double mean;
    private Double pulseRate;
    private Instant timestamp;

    public Double getDiastolic(){ return diastolic; }

    public Double getMean(){
        return mean;
    }

    public Double getPulseRate() {return pulseRate;}

    public Double getSystolic(){
        return systolic;
    }

    public Instant getTimestamp(){
        return timestamp;
    }

    public void setDiastolic(Double diastolic) { this.diastolic = diastolic; }

    public void setMean(Double mean) {  this.mean = mean;}

    public void setSystolic(Double systolic) { this.systolic = systolic; }

    public void setPulseRate(Double pulseRate) {
        this.pulseRate = pulseRate;
    }

    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

}
