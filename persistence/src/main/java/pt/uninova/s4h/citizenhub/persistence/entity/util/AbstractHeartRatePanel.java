package pt.uninova.s4h.citizenhub.persistence.entity.util;

public abstract class AbstractHeartRatePanel implements HeartRatePanel {

    private Double average;
    private Double maximum;
    private Double minimum;

    protected AbstractHeartRatePanel(Double average, Double maximum, Double minimum) {
        this.average = average;
        this.maximum = maximum;
        this.minimum = minimum;
    }

    public Double getAverage() {
        return average;
    }

    public Double getMaximum() {
        return maximum;
    }

    public Double getMinimum() {
        return minimum;
    }

    public void setAverage(Double value) {
        average = value;
    }

    public void setMaximum(Double value) {
        maximum = value;
    }

    public void setMinimum(Double value) {
        minimum = value;
    }
}


