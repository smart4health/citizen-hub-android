package pt.uninova.s4h.citizenhub.fhir.codesystem.loinc;

import care.data4life.fhir.r4.model.Coding;

public abstract class LoincCoding extends Coding {

    protected LoincCoding(String code, String display) {
        this.code = code;
        this.display = display;
        this.system = "http://loinc.org";
    }
}
