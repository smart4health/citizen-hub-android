package pt.uninova.s4h.citizenhub.fhir.codesystem.s4h;

import care.data4life.fhir.r4.model.Coding;

public abstract class Smart4HealthUserDocTypeCoding extends Coding {

    protected Smart4HealthUserDocTypeCoding(String code, String display) {
        this.code = code;
        this.display = display;
        this.system = "http://fhir.smart4health.eu/CodeSystem/s4h-user-doc-type";
    }
}
