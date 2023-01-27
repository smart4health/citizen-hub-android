package pt.uninova.s4h.citizenhub.fhir;

import care.data4life.fhir.r4.model.Coding;

public class GeneralReportCoding extends Coding {

    public GeneralReportCoding() {
        this.code = "general-report";
        this.display = "General report";
        this.system = "http://fhir.smart4health.eu/CodeSystem/s4h-user-doc-type";
    }
}
