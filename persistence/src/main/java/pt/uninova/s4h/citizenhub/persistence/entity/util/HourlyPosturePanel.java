package pt.uninova.s4h.citizenhub.persistence.entity.util;

public class HourlyPosturePanel extends AbstractPosturePanel implements Hourly {

    private Integer hourOfDay;

    public HourlyPosturePanel(Double correctPosture, Double incorrectPosture, Integer hourOfDay) {
        super(correctPosture, incorrectPosture);

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
