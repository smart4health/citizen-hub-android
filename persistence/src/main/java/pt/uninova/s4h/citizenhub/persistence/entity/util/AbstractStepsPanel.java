package pt.uninova.s4h.citizenhub.persistence.entity.util;

/** Contains the basic attributes of steps. */
public class AbstractStepsPanel implements StepsPanel {

    private Double steps;

    protected AbstractStepsPanel (Double steps){
        this.steps = steps;
    }

    @Override
    public Double getSteps() {
        return steps;
    }

    @Override
    public void setSteps(Double value) {
        steps = value;
    }

}
