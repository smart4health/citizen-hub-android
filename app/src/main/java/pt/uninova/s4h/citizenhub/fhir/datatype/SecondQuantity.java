package pt.uninova.s4h.citizenhub.fhir.datatype;

public class SecondQuantity extends UnifiedCodeForUnitsOfMeasureQuantity {

    public SecondQuantity(double value) {
        super(value, "s", "second");
    }
}
