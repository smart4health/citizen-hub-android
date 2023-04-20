package pt.uninova.s4h.citizenhub.fhir;

import java.util.Collections;

import care.data4life.fhir.r4.model.CodeSystemUDIEntryType;
import care.data4life.fhir.r4.model.CodeableConcept;
import care.data4life.fhir.r4.model.Identifier;
import pt.uninova.s4h.citizenhub.fhir.codesystem.hl7.SerialNumberCoding;

public class Device extends care.data4life.fhir.r4.model.Device {

    public Device(String serialNumber, String deviceIdentifier, String issuer) {
        final Identifier serialNumberIdentifier = new Identifier();

        serialNumberIdentifier.type = new CodeableConcept();
        serialNumberIdentifier.type.coding = Collections.singletonList(new SerialNumberCoding());
        serialNumberIdentifier.value = serialNumber;

        this.identifier = Collections.singletonList(serialNumberIdentifier);

        final DeviceUdiCarrier deviceUdiCarrier = new DeviceUdiCarrier();

        deviceUdiCarrier.deviceIdentifier = deviceIdentifier;
        deviceUdiCarrier.entryType = CodeSystemUDIEntryType.MANUAL;
        deviceUdiCarrier.issuer = issuer;

        this.udiCarrier = Collections.singletonList(deviceUdiCarrier);
    }

    public Device(String serialNumber, String deviceIdentifier, String issuer, String id) {
        this(serialNumber, deviceIdentifier, issuer);

        this.id = id;
    }
}
