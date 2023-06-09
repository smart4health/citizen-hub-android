package pt.uninova.s4h.citizenhub.persistence.entity.util;

/** Get and set functions for lumbar extension information. */
public interface LumbarExtensionPanel {

    Double getDuration();

    Double getRepetitions();

    Double getScore();

    Double getWeight();

    void setDurations(Double duration);

    void setRepetitions(Double repetitions);

    void setScore(Double score);

    void setWeight(Double weight);

}
