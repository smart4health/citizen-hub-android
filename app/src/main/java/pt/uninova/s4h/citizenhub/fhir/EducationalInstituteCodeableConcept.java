package pt.uninova.s4h.citizenhub.fhir;

import java.util.Collections;

import care.data4life.fhir.r4.model.CodeableConcept;
import pt.uninova.s4h.citizenhub.fhir.codesystem.hl7.EducationalInstituteCoding;

public class EducationalInstituteCodeableConcept extends CodeableConcept {

    public EducationalInstituteCodeableConcept() {
        this.coding = Collections.singletonList(new EducationalInstituteCoding());
    }

}
