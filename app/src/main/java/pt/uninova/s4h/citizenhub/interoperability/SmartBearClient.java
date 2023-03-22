package pt.uninova.s4h.citizenhub.interoperability;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ca.uhn.fhir.context.FhirContext;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class SmartBearClient implements Uploader<DailyPostureReport> {

    private final OkHttpClient httpClient;
    private final String url;
    private final String apiKey;

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    public SmartBearClient(String url, String apiKey) {
        this.url = url;
        this.apiKey = apiKey;

        httpClient = new OkHttpClient();
    }

    private String parseHourlyAggregate(int[] values) {
        final StringBuilder builder = new StringBuilder();

        builder.append(values[0]);

        for (int i = 1; i < values.length; i++) {
            builder.append(" ").append(values[i]);
        }

        return builder.toString();
    }

    @Override
    public void upload(DailyPostureReport content, Observer<Response> observer) {
        final Period effectivePeriod = new Period();

        effectivePeriod.setStart(Date.from(content.getStartTime()))
                .setEnd(Date.from(content.getEndTime()));

        final Reference subject = new Reference(String.format("Patient/%s", content.getPatientId()));

        final Quantity origin = new Quantity();

        origin.setCode("s")
                .setSystem("http://unitsofmeasure.org")
                .setUnit("second")
                .setValue(0);

        final Observation correctPostureObservation = new Observation();

        correctPostureObservation.getCode()
                .addCoding()
                .setCode("51093005")
                .setDisplay("Body posture normal")
                .setSystem("http://snomed.info/sct");

        correctPostureObservation.setEffective(effectivePeriod);
        correctPostureObservation.setId(IdType.newRandomUuid());
        correctPostureObservation.setStatus(Observation.ObservationStatus.FINAL);
        correctPostureObservation.setSubject(subject);

        correctPostureObservation.getValueSampledData()
                .setData(parseHourlyAggregate(content.getHourlyGoodPosture()))
                .setDimensions(1)
                .setLowerLimit(0)
                .setOrigin(origin)
                .setPeriod(3600000)
                .setUpperLimit(3600);

        final Observation incorrectPostureObservation = new Observation();

        incorrectPostureObservation.getCode()
                .addCoding()
                .setCode("249858009")
                .setDisplay("Poor posture")
                .setSystem("http://snomed.info/sct");

        incorrectPostureObservation.setEffective(effectivePeriod);
        incorrectPostureObservation.setId(IdType.newRandomUuid());
        incorrectPostureObservation.setStatus(Observation.ObservationStatus.FINAL);
        incorrectPostureObservation.setSubject(subject);

        incorrectPostureObservation.getValueSampledData()
                .setData(parseHourlyAggregate(content.getHourlyBadPosture()))
                .setDimensions(1)
                .setLowerLimit(0)
                .setOrigin(origin)
                .setPeriod(3600000)
                .setUpperLimit(3600);

        final DiagnosticReport diagnosticReport = new DiagnosticReport();

        diagnosticReport.getCode()
                .addCoding()
                .setCode("298341003")
                .setDisplay("Finding of body posture")
                .setSystem("http://snomed.info/sct");

        diagnosticReport.setEffective(effectivePeriod);

        diagnosticReport.addResult(new Reference(correctPostureObservation.getIdElement().getValue()));
        diagnosticReport.addResult(new Reference(incorrectPostureObservation.getIdElement().getValue()));

        diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
        diagnosticReport.setSubject(subject);
        diagnosticReport.getText()
                .setStatus(Narrative.NarrativeStatus.EMPTY);

        final Bundle bundle = new Bundle();

        bundle.setType(Bundle.BundleType.TRANSACTION);

        bundle.addEntry()
                .setFullUrl(correctPostureObservation.getIdElement().getValue())
                .setResource(correctPostureObservation)
                .getRequest()
                .setUrl("Observation")
                .setMethod(Bundle.HTTPVerb.POST);

        bundle.addEntry()
                .setFullUrl(incorrectPostureObservation.getIdElement().getValue())
                .setResource(incorrectPostureObservation)
                .getRequest()
                .setUrl("Observation")
                .setMethod(Bundle.HTTPVerb.POST);

        bundle.addEntry()
                .setResource(diagnosticReport)
                .getRequest()
                .setUrl("DiagnosticReport")
                .setMethod(Bundle.HTTPVerb.POST);

        final FhirContext fhirContext = FhirContext.forR4();
        final String body = fhirContext.newJsonParser().encodeResourceToString(bundle);

        final RequestBody reqBody = RequestBody.create(body, MediaType.get("application/json"));
        final Request request = new Request.Builder()
                .url(url)
                .header("apikey", apiKey)
                .post(reqBody)
                .build();

        executorService.execute(() -> {
            try (final Response response = httpClient.newCall(request).execute()) {
                observer.observe(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void upload(Collection<DailyPostureReport> content, Observer<Collection<Response>> observer) {
        final Collection<Response> responses = new ArrayList<>(content.size());

        for (DailyPostureReport i : content) {
            upload(i, value -> {
                synchronized (responses) {
                    responses.add(value);

                    if (responses.size() == content.size()) {
                        observer.observe(responses);
                    }
                }
            });
        }
    }
}
