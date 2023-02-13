package pt.uninova.s4h.citizenhub.persistence.entity.util;

/** Adds a specific day to a Steps Panel. */
public class DailyStepsPanel extends AbstractStepsPanel implements Daily {

    private Integer day;

    public DailyStepsPanel (Double steps, Integer day){
        super(steps);
        this.day = day;
    }

    @Override
    public Integer getDay() {
        return day;
    }

    @Override
    public void setDay(Integer value) {
        day = value;
    }
}
