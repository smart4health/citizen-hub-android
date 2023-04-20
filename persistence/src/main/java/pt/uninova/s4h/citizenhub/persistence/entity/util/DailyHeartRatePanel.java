package pt.uninova.s4h.citizenhub.persistence.entity.util;

/** Adds a specific day to a Heart Rate Panel. */
public class DailyHeartRatePanel extends AbstractHeartRatePanel implements Daily {

    private Integer day;

    public DailyHeartRatePanel(Double average, Double maximum, Double minimum, Integer day) {
        super(average, maximum, minimum);

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
