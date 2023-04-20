package pt.uninova.s4h.citizenhub.fhir.codesystem.hl7;

import care.data4life.fhir.r4.model.Coding;

public abstract class IdentifierTypeCoding extends Coding {

    public IdentifierTypeCoding(String code, String display) {
        this.code = code;
        this.display = display;
        this.system = "http://terminology.hl7.org/CodeSystem/v2-0203";
    }
}
