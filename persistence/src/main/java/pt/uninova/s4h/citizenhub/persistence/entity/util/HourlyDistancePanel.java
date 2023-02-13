package pt.uninova.s4h.citizenhub.persistence.entity.util;

/** Adds the hour of the day to a Distance Panel. */
public class HourlyDistancePanel extends AbstractDistancePanel implements Hourly {

    private Integer hourOfDay;

    public HourlyDistancePanel (Double distance, Integer hourOfDay){
        super(distance);
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
