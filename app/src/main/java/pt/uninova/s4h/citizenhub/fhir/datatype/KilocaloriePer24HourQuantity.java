package pt.uninova.s4h.citizenhub.fhir.datatype;

public class KilocaloriePer24HourQuantity extends UnifiedCodeForUnitsOfMeasureQuantity {

    public KilocaloriePer24HourQuantity(double value) {
        super(value, "kcal/(24.h)", "Kilocalorie per 24 hour");
    }
}
