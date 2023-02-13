package pt.uninova.s4h.citizenhub.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import care.data4life.fhir.r4.model.Attachment;
import care.data4life.fhir.r4.model.CodeSystemDocumentReferenceStatus;
import care.data4life.fhir.r4.model.CodeableConcept;
import care.data4life.fhir.r4.model.Coding;
import care.data4life.fhir.r4.model.DocumentReference;
import care.data4life.fhir.r4.model.FhirDate;
import care.data4life.fhir.r4.model.FhirDateTime;
import care.data4life.fhir.r4.model.FhirInstant;
import care.data4life.fhir.r4.model.FhirTime;
import care.data4life.fhir.r4.model.Organization;
import care.data4life.sdk.Data4LifeClient;
import care.data4life.sdk.SdkContract;
import care.data4life.sdk.call.Callback;
import care.data4life.sdk.call.Fhir4Record;
import care.data4life.sdk.helpers.r4.AttachmentBuilder;
import care.data4life.sdk.helpers.r4.DocumentReferenceBuilder;
import care.data4life.sdk.helpers.r4.OrganizationBuilder;
import care.data4life.sdk.lang.D4LException;
import care.data4life.sdk.listener.ResultListener;
import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.localization.MeasurementKindLocalization;
import pt.uninova.s4h.citizenhub.persistence.repository.ReportRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.Smart4HealthDailyReportRepository;
import pt.uninova.s4h.citizenhub.report.PDFDailyReport;
import pt.uninova.s4h.citizenhub.report.Report;
import pt.uninova.s4h.citizenhub.report.ReportGenerator;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class Smart4HealthPdfUploader extends ListenableWorker {

    public Smart4HealthPdfUploader(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }


    private Organization getAuthor() {
        return OrganizationBuilder.buildWith(
                getOrganizationType(),
                "UNINOVA - Instituto de Desenvolvimento de Novas Tecnologias",
                "Faculdade de Ciências e Tecnologia",
                "2829-516",
                "Caparica",
                "(+351) 21 29 48 527",
                "https://www.uninova.pt/");
    }

    private CodeableConcept getGeneralReportCodeableConcept() {
        final Coding coding = new Coding();

        coding.code = "general-report";
        coding.display = "General report";
        coding.system = "http://fhir.smart4health.eu/CodeSystem/s4h-user-doc-type";

        final CodeableConcept concept = new CodeableConcept();

        concept.coding = Collections.singletonList(coding);

        return concept;
    }

    private CodeableConcept getOrganizationType() {
        final Coding organizationType = new Coding();

        organizationType.code = "edu";

        final CodeableConcept codeableConcept = new CodeableConcept();

        codeableConcept.coding = Collections.singletonList(organizationType);

        return codeableConcept;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            final Data4LifeClient client = Data4LifeClient.getInstance();

            client.isUserLoggedIn(new ResultListener<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    if (result) {
                        final Smart4HealthDailyReportRepository smart4HealthDailyReportRepository = new Smart4HealthDailyReportRepository(getApplicationContext());

                        final LocalDateTime now = LocalDateTime.now();

                        final FhirDate date = new FhirDate(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
                        final FhirTime time = new FhirTime(now.getHour(), now.getMinute(), now.getSecond(), null, null);
                        final FhirDateTime dateTime = new FhirDateTime(date, time, TimeZone.getDefault());

                        final MeasurementKindLocalization measurementKindLocalization = new MeasurementKindLocalization(getApplicationContext());

                        smart4HealthDailyReportRepository.readDaysWithData(value -> {
                            for (LocalDate i : value) {
                                if (i.compareTo(now.toLocalDate()) < 0) {
                                    final PDFDailyReport dailyReportGeneratorPDF = new PDFDailyReport(getApplicationContext());

                                    final Observer<byte[]> observer = bytes -> {
                                        final SdkContract.Fhir4RecordClient fhirClient = client.getFhir4();
                                        final List<Attachment> attachments = new ArrayList<>(1);

                                        try {
                                            final Attachment attachment = AttachmentBuilder.buildWith(now.toString(), dateTime, "application/pdf", bytes);
                                            attachments.add(attachment);

                                            final DocumentReference documentReference = DocumentReferenceBuilder.buildWith(
                                                    getApplicationContext().getResources().getString(R.string.report_title, i.format(DateTimeFormatter.ISO_DATE)),
                                                    CodeSystemDocumentReferenceStatus.CURRENT,
                                                    attachments,
                                                    getGeneralReportCodeableConcept(),
                                                    getAuthor(),
                                                    null);


                                            final FhirDate dt = new FhirDate(i.getYear(), i.getMonthValue(), i.getDayOfMonth());
                                            final FhirTime tm = new FhirTime(0, 0, 0, null, null);
                                            final FhirDateTime dttm = new FhirDateTime(dt, tm, TimeZone.getDefault());

                                            documentReference.date = new FhirInstant(dttm);

                                            fhirClient.create(documentReference, new ArrayList<>(), new Callback<Fhir4Record<DocumentReference>>() {
                                                @Override
                                                public void onSuccess(Fhir4Record<DocumentReference> fhir4Record) {
                                                    smart4HealthDailyReportRepository.createOrUpdatePdfUTC(i, true);
                                                }

                                                @Override
                                                public void onError(@NonNull D4LException e) {
                                                    e.printStackTrace();
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    };

                                    ReportRepository reportRepository = new ReportRepository(getApplicationContext());
                                    ReportGenerator reportGenerator = new ReportGenerator(getApplicationContext());
                                    Observer<Report> observerWorkTimeReport = workTimeReport -> {
                                        Observer<Report> observerNotWorkTimeReport = notWorkTimeReport -> {
                                            if(workTimeReport.getGroups().size() > 0 || notWorkTimeReport.getGroups().size() > 0) {
                                                dailyReportGeneratorPDF.generateCompleteReport(workTimeReport, notWorkTimeReport, getApplicationContext().getResources(), i, measurementKindLocalization, observer);
                                                //dailyReportGeneratorPDF.generateNotWorkTimeReportPDF(observer, getResources(), new ReportRepository(getContext()), model.getCurrentDate(), measurementKindLocalization);
                                                //dailyReportGeneratorPDF.generateWorkTimeReportPDF(observer, getResources(), new ReportRepository(getContext()), model.getCurrentDate(), measurementKindLocalization);
                                            }
                                        };
                                        reportGenerator.generateNotWorkTimeReport(reportRepository, i, true, observerNotWorkTimeReport);
                                    };
                                    reportGenerator.generateWorkTimeReport(reportRepository, i, true, observerWorkTimeReport);

                                }
                            }

                            completer.set(Result.success());
                        });
                    } else {
                        completer.set(Result.success());
                    }
                }

                @Override
                public void onError(@NonNull D4LException e) {
                    completer.set(Result.failure());
                }
            });

            return this.toString();
        });
    }
}
