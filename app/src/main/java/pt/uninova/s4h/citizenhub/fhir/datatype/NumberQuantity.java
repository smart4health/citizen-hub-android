package pt.uninova.s4h.citizenhub.fhir.datatype;

public class NumberQuantity extends UnifiedCodeForUnitsOfMeasureQuantity {

    public NumberQuantity(double value) {
        super(value, "{#}", "Number");
    }
}
