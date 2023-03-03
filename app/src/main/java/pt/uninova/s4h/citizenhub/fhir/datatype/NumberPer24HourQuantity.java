package pt.uninova.s4h.citizenhub.fhir.datatype;

import care.data4life.fhir.r4.model.Quantity;

public class NumberPer24HourQuantity extends UnifiedCodeForUnitsOfMeasureQuantity {

    public NumberPer24HourQuantity(double value) {
        super(value, "{#}/(24.h)", "Number per 24 hour");
    }
}
