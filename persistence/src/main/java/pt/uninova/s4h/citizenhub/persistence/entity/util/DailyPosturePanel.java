package pt.uninova.s4h.citizenhub.persistence.entity.util;

public class DailyPosturePanel extends AbstractPosturePanel implements Daily {

    private Integer day;

    public DailyPosturePanel(Double correctPosture, Double incorrectPosture, Integer day) {
        super(correctPosture, incorrectPosture);

        this.day = day;
    }

    @Override
    public Integer getDay() {
        return day;
    }

    @Override
    public void setDay(Integer day) {
        this.day = day;
    }

}
