package pt.uninova.s4h.citizenhub.fhir;

import java.time.LocalDate;
import java.util.Collections;

import care.data4life.fhir.r4.model.CodeSystemObservationStatus;
import care.data4life.fhir.r4.model.CodeableConcept;
import care.data4life.fhir.r4.model.FhirDate;
import care.data4life.fhir.r4.model.FhirDateTime;
import care.data4life.fhir.r4.model.Observation;
import care.data4life.fhir.r4.model.Period;
import pt.uninova.s4h.citizenhub.fhir.codesystem.hl7.VitalSignsCoding;
import pt.uninova.s4h.citizenhub.fhir.datatype.HeartBeatsPerMinuteQuantity;

public abstract class HeartRate24HourObservation extends Observation {

    protected HeartRate24HourObservation(LocalDate date, double value) {
        super(CodeSystemObservationStatus.FINAL, new CodeableConcept());

        final CodeableConcept categoryCodeableConcept = new CodeableConcept();

        categoryCodeableConcept.coding = Collections.singletonList(new VitalSignsCoding());

        this.category = Collections.singletonList(categoryCodeableConcept);

        this.effectivePeriod = new Period();
        this.effectivePeriod.start = new FhirDateTime(new FhirDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth()), null, null);
        this.effectivePeriod.end = this.effectivePeriod.start;
        this.valueQuantity = new HeartBeatsPerMinuteQuantity(value);
    }
}
