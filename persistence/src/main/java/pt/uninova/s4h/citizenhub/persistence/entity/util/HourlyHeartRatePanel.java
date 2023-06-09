package pt.uninova.s4h.citizenhub.persistence.entity.util;

/** Adds the hour of the day to a Heart Rate Panel. */
public class HourlyHeartRatePanel extends AbstractHeartRatePanel implements Hourly {

    private Integer hourOfDay;

    public HourlyHeartRatePanel(Double average, Double maximum, Double minimum, Integer hourOfDay) {
        super(average, maximum, minimum);

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
