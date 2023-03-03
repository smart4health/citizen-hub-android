package pt.uninova.s4h.citizenhub.fhir.datatype;

public class MetrePer24HourQuantity extends UnifiedCodeForUnitsOfMeasureQuantity {

    public MetrePer24HourQuantity(double value) {
        super(value, "m/(24.h)", "Metre per 24 hour");
    }
}
