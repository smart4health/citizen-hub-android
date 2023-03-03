package pt.uninova.s4h.citizenhub.fhir.datatype;

import java.math.BigDecimal;

import care.data4life.fhir.r4.model.FhirDecimal;
import care.data4life.fhir.r4.model.Quantity;

public abstract class UnifiedCodeForUnitsOfMeasureQuantity extends Quantity {

    protected UnifiedCodeForUnitsOfMeasureQuantity(double value, String code, String unit) {
        this.code = code;
        this.system = "http://unitsofmeasure.org";
        this.unit = unit;
        this.value = new FhirDecimal(new BigDecimal(value));
    }
}
