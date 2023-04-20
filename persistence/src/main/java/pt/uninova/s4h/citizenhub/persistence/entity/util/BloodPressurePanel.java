package pt.uninova.s4h.citizenhub.persistence.entity.util;

/** Get and set functions for blood pressure information. */
public interface BloodPressurePanel {

    Double getDiastolic();

    Double getMean();

    Double getSystolic();

    void setDiastolic(Double diastolic);

    void setMean(Double mean);

    void setSystolic(Double systolic);

}
