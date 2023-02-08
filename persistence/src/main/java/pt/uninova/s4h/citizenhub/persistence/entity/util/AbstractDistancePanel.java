package pt.uninova.s4h.citizenhub.persistence.entity.util;

public class AbstractDistancePanel implements DistancePanel {

    private Double distance;

    protected AbstractDistancePanel (Double distance){
        this.distance = distance;
    }

    @Override
    public Double getDistance() {
        return distance;
    }

    @Override
    public void setDistance(Double value) {
        distance = value;
    }
}
