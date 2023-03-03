package pt.uninova.s4h.citizenhub.fhir;

import java.util.Arrays;
import java.util.Collections;

import care.data4life.fhir.r4.model.Address;
import care.data4life.fhir.r4.model.CodeSystemContactPointSystem;
import care.data4life.fhir.r4.model.CodeableConcept;
import care.data4life.fhir.r4.model.ContactPoint;
import care.data4life.fhir.r4.model.Organization;
import pt.uninova.s4h.citizenhub.fhir.codesystem.hl7.EducationalInstituteCoding;

public class UninovaOrganization extends Organization {

    public UninovaOrganization() {
        final Address address = new Address();

        address.city = "Caparica";
        address.line = Collections.singletonList("Faculdade de Ciências e Tecnologia");
        address.postalCode = "2829-516";

        final ContactPoint phone = new ContactPoint();

        phone.system = CodeSystemContactPointSystem.PHONE;
        phone.value = "(+351) 212 948 527";

        final ContactPoint url = new ContactPoint();

        url.system = CodeSystemContactPointSystem.URL;
        url.value = "https://www.uninova.pt/";

        this.address = Collections.singletonList(address);
        this.name = "UNINOVA - Instituto de Desenvolvimento de Novas Tecnologias";
        this.telecom = Arrays.asList(phone, url);

        final CodeableConcept typeCodeableConcept = new CodeableConcept();

        typeCodeableConcept.coding = Collections.singletonList(new EducationalInstituteCoding());

        this.type = Collections.singletonList(typeCodeableConcept);
    }

    public UninovaOrganization(String id) {
        this();

        this.id = id;
    }
}
