package pt.uninova.s4h.citizenhub.fhir;

import java.time.LocalDate;
import java.util.Collections;

import pt.uninova.s4h.citizenhub.fhir.codesystem.loinc.HeartRate24HourMaximumCoding;

public class HeartRate24HourMaximumObservation extends HeartRate24HourObservation {

    public HeartRate24HourMaximumObservation(LocalDate date, double value) {
        super(date, value);

        this.code.coding = Collections.singletonList(new HeartRate24HourMaximumCoding());
    }
}