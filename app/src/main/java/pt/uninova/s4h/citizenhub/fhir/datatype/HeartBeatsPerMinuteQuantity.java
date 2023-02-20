package pt.uninova.s4h.citizenhub.fhir.datatype;

public class HeartBeatsPerMinuteQuantity extends UnifiedCodeForUnitsOfMeasureQuantity {

    public HeartBeatsPerMinuteQuantity(double value) {
        super(value, "{beats}/min", "Heart beats per minute");
    }
}
