package pt.uninova.s4h.citizenhub.persistence.entity.util;

/** Adds a specific day to a Distance Panel. */
public class DailyDistancePanel extends AbstractDistancePanel implements Daily {

    private Integer day;

    public DailyDistancePanel (Double distance, Integer day){
        super(distance);
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
