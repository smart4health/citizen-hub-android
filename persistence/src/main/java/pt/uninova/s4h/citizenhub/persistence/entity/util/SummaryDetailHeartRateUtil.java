package pt.uninova.s4h.citizenhub.persistence.entity.util;

public class SummaryDetailHeartRateUtil {

    private Double maximum;
    private Double minimum;
    private Double average;
    private Double time;

    public Double getMaximum(){
        return maximum;
    }

    public void setMaximum(Double maximum) { this.maximum = maximum; }

    public Double getMinimum(){ return minimum; }

    public void setMinimum(Double minimum) { this.minimum = minimum; }

    public Double getAverage(){
        return average;
    }

    public void setAverage(Double average) { this.average = average; }

    public Double getTime(){
        return time;
    }

    public void setTime(Double time) { this.time = time; }

}
