package pt.uninova.s4h.citizenhub.fhir;

import care.data4life.fhir.r4.model.Coding;

public class EducationalInstituteCoding extends Coding {

    public EducationalInstituteCoding() {
        this.code = "edu";
        this.display = "Educational Institute";
        this.system = "http://terminology.hl7.org/CodeSystem/organization-type";
    }

}
