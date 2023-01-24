package pt.uninova.s4h.citizenhub.persistence.entity.util;

public class DailyBloodPressurePanel extends AbstractBloodPressurePanel implements Daily {

    private Integer day;

    public DailyBloodPressurePanel(Double diastolic, Double mean, Double systolic, Integer day) {
        super(diastolic, mean, systolic);

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
