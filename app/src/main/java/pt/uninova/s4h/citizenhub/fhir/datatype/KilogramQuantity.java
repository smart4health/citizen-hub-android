package pt.uninova.s4h.citizenhub.fhir.datatype;

public class KilogramQuantity extends UnifiedCodeForUnitsOfMeasureQuantity {

    public KilogramQuantity(double value) {
        super(value, "kg", "kilogram");
    }
}
