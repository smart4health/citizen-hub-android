package pt.uninova.s4h.citizenhub.persistence.entity.util;

public class HourlyCaloriesPanel extends AbstractCaloriesPanel implements Hourly {

    private Integer hourOfDay;

    public HourlyCaloriesPanel (Double calories, Integer hourOfDay) {
        super(calories);
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
