package pt.uninova.s4h.citizenhub.persistence;

import androidx.room.TypeConverters;

public class MeasurementAggregate {

    private MeasurementKind measurementKind;
    private Double average;
    private Integer count;
    private Double max;
    private Double min;
    private Double sum;

    public MeasurementKind getMeasurementKind() {
        return measurementKind;
    }

    public void setMeasurementKind(MeasurementKind measurementKind) {
        this.measurementKind = measurementKind;
    }

    public Double getAverage() {
        return average;
    }

    public void setAverage(Double average) {
        this.average = average;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }
}
