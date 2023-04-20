package pt.uninova.s4h.citizenhub.persistence.entity.util;

/** Get and set functions for posture information. */
public interface PosturePanel {

    Double getCorrectPosture();

    Double getIncorrectPosture();

    void setCorrectPosture(Double value);

    void setIncorrectPosture(Double value);

}
