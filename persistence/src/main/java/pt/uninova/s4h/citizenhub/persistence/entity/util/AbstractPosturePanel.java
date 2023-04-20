package pt.uninova.s4h.citizenhub.persistence.entity.util;

/** Contains the basic attributes of posture. */
public class AbstractPosturePanel implements PosturePanel {

    private Double correctPosture;
    private Double incorrectPosture;

    protected AbstractPosturePanel(Double correctPosture, Double incorrectPosture){
        this.correctPosture = correctPosture;
        this.incorrectPosture = incorrectPosture;
    }

    @Override
    public Double getCorrectPosture() {
        return correctPosture;
    }

    @Override
    public Double getIncorrectPosture() {
        return incorrectPosture;
    }

    @Override
    public void setCorrectPosture(Double value) {
        correctPosture = value;
    }

    @Override
    public void setIncorrectPosture(Double value) {
        incorrectPosture = value;
    }
}
