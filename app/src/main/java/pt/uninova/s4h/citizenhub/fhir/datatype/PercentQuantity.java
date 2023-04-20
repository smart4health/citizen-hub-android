package pt.uninova.s4h.citizenhub.fhir.datatype;

public class PercentQuantity extends  UnifiedCodeForUnitsOfMeasureQuantity{

    public PercentQuantity(double value) {
        super(value, "%", "% percent");
    }
}
