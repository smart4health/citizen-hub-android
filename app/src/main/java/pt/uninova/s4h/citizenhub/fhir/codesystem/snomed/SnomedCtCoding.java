package pt.uninova.s4h.citizenhub.fhir.codesystem.snomed;

import care.data4life.fhir.r4.model.Coding;

public abstract class SnomedCtCoding extends Coding {

    protected SnomedCtCoding(String code, String display) {
        this.code = code;
        this.display = display;
        this.system = "http://snomed.info/sct";
    }
}
