package pt.uninova.s4h.citizenhub.persistence.entity.util;

public class DailyCaloriesPanel extends AbstractCaloriesPanel implements Daily {

    private Integer day;

    public DailyCaloriesPanel (Double calories, Integer day) {
        super(calories);
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
