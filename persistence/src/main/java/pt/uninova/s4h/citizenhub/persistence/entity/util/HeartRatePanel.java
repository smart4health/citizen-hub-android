package pt.uninova.s4h.citizenhub.persistence.entity.util;

public interface HeartRatePanel {

    Double getAverage();

    Double getMaximum();

    Double getMinimum();

    void setAverage(Double value);

    void setMaximum(Double value);

    void setMinimum(Double value);

}
