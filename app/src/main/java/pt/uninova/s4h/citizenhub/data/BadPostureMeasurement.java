package pt.uninova.s4h.citizenhub.data;

public class BadPostureMeasurement extends Measurement<Double> {

    public BadPostureMeasurement(Double value) {
        super(POSTURE_INCORRECT, value);
    }
}
