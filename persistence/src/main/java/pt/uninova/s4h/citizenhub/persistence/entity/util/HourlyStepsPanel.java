package pt.uninova.s4h.citizenhub.persistence.entity.util;

public class HourlyStepsPanel extends AbstractStepsPanel implements Hourly {

    private Integer hourOfDay;

    public HourlyStepsPanel (Double steps, Integer hourOfDay) {
        super(steps);
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
