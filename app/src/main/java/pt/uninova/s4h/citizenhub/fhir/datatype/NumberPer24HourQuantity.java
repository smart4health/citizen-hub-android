package pt.uninova.s4h.citizenhub.fhir.datatype;

public class NumberPer24HourQuantity extends UnifiedCodeForUnitsOfMeasureQuantity {

    public NumberPer24HourQuantity(double value) {
        super(value, "{#}/(24.h)", "Number per 24 hour");
    }
}
