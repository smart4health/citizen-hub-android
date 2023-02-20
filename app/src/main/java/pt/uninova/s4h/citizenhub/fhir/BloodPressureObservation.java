package pt.uninova.s4h.citizenhub.fhir;

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
import care.data4life.fhir.r4.model.FhirTime;
import care.data4life.fhir.r4.model.Observation;
import care.data4life.sdk.helpers.r4.FhirHelpers;
import care.data4life.sdk.util.StringUtils;
import pt.uninova.s4h.citizenhub.fhir.codesystem.hl7.VitalSignsCoding;
import pt.uninova.s4h.citizenhub.fhir.codesystem.loinc.BloodPressurePanelWithAllChildrenOptionalCoding;
import pt.uninova.s4h.citizenhub.fhir.codesystem.loinc.DiastolicBloodPressureCoding;
import pt.uninova.s4h.citizenhub.fhir.codesystem.loinc.MeanBloodPressureCoding;
import pt.uninova.s4h.citizenhub.fhir.codesystem.loinc.SystolicBloodPressureCoding;
import pt.uninova.s4h.citizenhub.fhir.datatype.MillimetreOfMercuryQuantity;

public class BloodPressureObservation extends Observation {

    private static class SystolicBloodPressureComponent extends ObservationComponent {

        public SystolicBloodPressureComponent(double value) {
            super(new CodeableConcept());

            this.code.coding = Collections.singletonList(new SystolicBloodPressureCoding());
            this.valueQuantity = new MillimetreOfMercuryQuantity(value);
        }
    }

    private static class DiastolicBloodPressureComponent extends ObservationComponent {

        public DiastolicBloodPressureComponent(double value) {
            super(new CodeableConcept());

            this.code.coding = Collections.singletonList(new DiastolicBloodPressureCoding());
            this.valueQuantity = new MillimetreOfMercuryQuantity(value);
        }
    }

    private static class MeanBloodPressureComponent extends ObservationComponent {

        public MeanBloodPressureComponent(double value) {
            super(new CodeableConcept());

            this.code.coding = Collections.singletonList(new MeanBloodPressureCoding());
            this.valueQuantity = new MillimetreOfMercuryQuantity(value);
        }
    }

    public BloodPressureObservation(Instant timestamp, double systolic, double diastolic, double mean) {
        super(CodeSystemObservationStatus.FINAL, new CodeableConcept());

        final CodeableConcept categoryCodeableConcept = new CodeableConcept();

        categoryCodeableConcept.coding = Collections.singletonList(new VitalSignsCoding());

        this.category = Collections.singletonList(categoryCodeableConcept);
        this.code.coding = Collections.singletonList(new BloodPressurePanelWithAllChildrenOptionalCoding());
        this.component = Arrays.asList(new SystolicBloodPressureComponent(systolic), new DiastolicBloodPressureComponent(diastolic), new MeanBloodPressureComponent(mean));

        final LocalDateTime localDateTime = LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault());

        this.effectiveDateTime = new FhirDateTime(new FhirDate(localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth()), new FhirTime(localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond(), null, null), TimeZone.getTimeZone(ZoneId.systemDefault()));
        this.performer = Collections.singletonList(FhirHelpers.contain(this, new UninovaOrganization(StringUtils.randomUUID())));
    }

    public BloodPressureObservation(Instant timestamp, double systolic, double diastolic, double mean, Device device) {
        this(timestamp, systolic, diastolic, mean);

        this.device = FhirHelpers.contain(this, device);
    }
}
