package pt.uninova.s4h.citizenhub.persistence.entity.util;

/** Contains the basic attributes of lumbar extension training. */
public class AbstractLumbarExtensionPanel implements LumbarExtensionPanel {

    private Double duration;
    private Double repetitions;
    private Double score;
    private Double weight;

    AbstractLumbarExtensionPanel(Double duration, Double repetitions, Double score, Double weight){
        this.duration = duration;
        this.repetitions = repetitions;
        this.score = score;
        this.weight = weight;
    }


    @Override
    public Double getDuration() {
        return duration;
    }

    @Override
    public Double getRepetitions() {
        return repetitions;
    }

    @Override
    public Double getScore() {
        return score;
    }

    @Override
    public Double getWeight() {
        return weight;
    }

    @Override
    public void setDurations(Double duration) {
        this.duration = duration;
    }

    @Override
    public void setRepetitions(Double repetitions) {
        this.repetitions = repetitions;
    }

    @Override
    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
