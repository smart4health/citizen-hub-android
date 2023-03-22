package pt.uninova.s4h.citizenhub.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.UUID;

import care.data4life.fhir.r4.model.Observation;
import care.data4life.sdk.Data4LifeClient;
import care.data4life.sdk.SdkContract;
import care.data4life.sdk.call.Callback;
import care.data4life.sdk.call.Fhir4Record;
import care.data4life.sdk.lang.D4LException;
import pt.uninova.s4h.citizenhub.fhir.BloodPressureObservation;
import pt.uninova.s4h.citizenhub.fhir.Device;
import pt.uninova.s4h.citizenhub.persistence.repository.BloodPressureMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.SampleRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.SettingRepository;

public class BloodPressureUploader extends ListenableWorker {

    private final long sampleId;

    public BloodPressureUploader(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);

        sampleId = workerParams.getInputData().getLong("sampleId", -1);
    }

    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            final BloodPressureMeasurementRepository bloodPressureMeasurementRepository = new BloodPressureMeasurementRepository(getApplicationContext());
            final SampleRepository sampleRepository = new SampleRepository(getApplicationContext());
            final SettingRepository settingRepository = new SettingRepository(getApplicationContext());

            sampleRepository.read(sampleId, sample -> {
                if (sample == null) {
                    completer.set(Result.success());
                } else {
                    bloodPressureMeasurementRepository.readBySample(sampleId, bloodPressureMeasurement -> {
                        if (bloodPressureMeasurement == null) {
                            completer.set(Result.success());
                        } else {

                            settingRepository.readBySample(sampleId, settings -> {
                                final String sn = settings.get("udi-serial-number");
                                final String sys = settings.get("udi-system");
                                final String id = settings.get("udi-device-identifier");

                                BloodPressureObservation observation;

                                if (sn != null && sys != null && id != null) {
                                    final Device device = new Device(sn, id, sys, UUID.randomUUID().toString());

                                    observation = new BloodPressureObservation(sample.getTimestamp(), bloodPressureMeasurement.getSystolic(), bloodPressureMeasurement.getDiastolic(), bloodPressureMeasurement.getMeanArterialPressure(), device);
                                } else {
                                    observation = new BloodPressureObservation(sample.getTimestamp(), bloodPressureMeasurement.getSystolic(), bloodPressureMeasurement.getDiastolic(), bloodPressureMeasurement.getMeanArterialPressure());
                                }

                                final SdkContract.Fhir4RecordClient client = Data4LifeClient.getInstance().getFhir4();

                                client.create(observation, new ArrayList<>(), new Callback<Fhir4Record<Observation>>() {
                                    @Override
                                    public void onSuccess(Fhir4Record<Observation> fhir4Record) {
                                        completer.set(Result.success());
                                    }

                                    @Override
                                    public void onError(@NonNull D4LException e) {
                                        e.printStackTrace();
                                        completer.set(Result.retry());
                                    }
                                });
                            });
                        }
                    });
                }
            });

            return this.toString();
        });
    }
}
