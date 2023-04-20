package pt.uninova.s4h.citizenhub.persistence.entity.util;

/** Get and set functions for heart rate information. */
public interface HeartRatePanel {

    Double getAverage();

    Double getMaximum();

    Double getMinimum();

    void setAverage(Double value);

    void setMaximum(Double value);

    void setMinimum(Double value);

}
