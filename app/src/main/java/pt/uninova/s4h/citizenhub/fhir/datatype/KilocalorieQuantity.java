package pt.uninova.s4h.citizenhub.fhir.datatype;

public class KilocalorieQuantity extends UnifiedCodeForUnitsOfMeasureQuantity {

    public KilocalorieQuantity(double value) {
        super(value, "kcal", "Kilocalorie");
    }
}
