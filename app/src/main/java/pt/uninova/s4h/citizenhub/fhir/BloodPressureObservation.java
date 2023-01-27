package pt.uninova.s4h.citizenhub.fhir;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.TimeZone;

import care.data4life.fhir.r4.model.CodeSystemObservationStatus;
import care.data4life.fhir.r4.model.CodeableConcept;
import care.data4life.fhir.r4.model.FhirDate;
import care.data4life.fhir.r4.model.FhirDateTime;
import care.data4life.fhir.r4.model.FhirDecimal;
import care.data4life.fhir.r4.model.FhirTime;
import care.data4life.fhir.r4.model.Observation;
import care.data4life.fhir.r4.model.Quantity;
import pt.uninova.s4h.citizenhub.fhir.codesystem.hl7.VitalSignsCoding;
import pt.uninova.s4h.citizenhub.fhir.codesystem.loinc.BloodPressurePanelWithAllChildrenOptionalCoding;
import pt.uninova.s4h.citizenhub.fhir.codesystem.loinc.SystolicBloodPressureCoding;

public class BloodPressureObservation extends Observation {

    private static class SystolicBloodPressureComponent extends ObservationComponent {

        public SystolicBloodPressureComponent(double value) {
            super(new CodeableConcept());

            this.code.coding = Collections.singletonList(new SystolicBloodPressureCoding());
            this.valueQuantity = new Quantity();

            this.valueQuantity.code = "mm[Hg]";
            this.valueQuantity.unit = "mm Hg";
            this.valueQuantity.system = "http://unitsofmeasure.org";
            this.valueQuantity.value = new FhirDecimal(new BigDecimal(value));
        }
    }

    public BloodPressureObservation(Instant timestamp, double systolic, double diastolic, double mean) {
        super(CodeSystemObservationStatus.FINAL, new CodeableConcept());

        this.code.coding = Collections.singletonList(new BloodPressurePanelWithAllChildrenOptionalCoding());

        final LocalDateTime localDateTime = LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault());

        this.effectiveDateTime = new FhirDateTime(new FhirDate(localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth()), new FhirTime(localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond(), null, null), TimeZone.getTimeZone(ZoneId.systemDefault()));


        this.component = Arrays.asList();
    }
}
