package pt.uninova.s4h.citizenhub.fhir.codesystem.s4h;

import care.data4life.fhir.r4.model.Coding;

public abstract class LumbarExtensionCoding extends Coding {

    protected LumbarExtensionCoding(String code, String display) {
        this.code = code;
        this.display = display;
        this.system = "https://canonical.smart4health.grisenergia.pt/fhir/code-system/lumbar-extension";
    }
}
