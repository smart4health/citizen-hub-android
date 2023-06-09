package pt.uninova.s4h.citizenhub.persistence.entity.util;

/** Adds the hour of the day to a Blood Pressure Panel. */
public class HourlyBloodPressurePanel extends AbstractBloodPressurePanel implements Hourly{

    private Integer hourOfDay;

    public HourlyBloodPressurePanel(Double diastolic, Double mean, Double systolic, Integer hourOfDay) {
        super(diastolic, mean, systolic);

        this.hourOfDay = hourOfDay;
    }

    @Override
    public Integer getHourOfDay() {
        return hourOfDay;
    }

    @Override
    public void setHourOfDay(Integer hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

}
