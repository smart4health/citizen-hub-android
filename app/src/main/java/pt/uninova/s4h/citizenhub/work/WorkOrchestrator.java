package pt.uninova.s4h.citizenhub.work;

import android.content.Context;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import pt.uninova.s4h.citizenhub.data.Sample;

public class WorkOrchestrator {

    private final WorkManager workManager;

    public WorkOrchestrator(WorkManager workManager) {
        this.workManager = workManager;
        //workManager.cancelAllWork();
    }

    public void enqueueSmartBearUploader() {
        addPeriodicWork(SmartBearUploader.class, "smartbearuploader", 12, TimeUnit.HOURS);
    }

    public void cancelSmartBearUploader(){
        cancelWork("smartbearuploader");
    }

    public void enqueueSmart4HealthUploader() {
        addPeriodicWork(Smart4HealthPdfUploader.class, "smart4healthuploader", 12, TimeUnit.HOURS);
    }

    public void enqueueSmart4HealthWeeklyUploader() {
        addPeriodicWork(Smart4HealthWeeklyPDFUploader.class,"smart4healthweeklyuploader", 24, TimeUnit.HOURS);
    }

    public void enqueueSmart4HealthMonthlyUploader(){
        addPeriodicWork(Smart4HealthMonthlyPDFUploader.class,"smart4healthmonthlyuploader", 24, TimeUnit.HOURS);
    }

    public void cancelSmart4HealthUploader(){
        cancelWork("smart4healthuploader");
    }

    public void cancelSmart4HealthWeeklyUploader(){
        cancelWork("smart4healthweeklyuploader");
    }

    public void cancelSmart4HealthMonthlyUploader(){
        cancelWork("smart4healthmonthlyuploader");
    }


    public void enqueueSmart4HealthUniqueWorkBloodPressure(Context context, long sampleId){
        final WorkManager workManager = WorkManager.getInstance(context);
        final Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        final Data data = new Data.Builder()
                .putLong("sampleId", sampleId)
                .build();

        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(BloodPressureUploader.class)
                .setInputData(data)
                .setConstraints(constraints)
                .build();

        workManager.enqueueUniqueWork("smart4health_pdf_" + sampleId, ExistingWorkPolicy.APPEND_OR_REPLACE, workRequest);
    }

    public void enqueueSmart4HealthUniqueWorkLumbarExtension(Context context, long sampleId){
        final WorkManager workManager = WorkManager.getInstance(context);
        final Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        final Data data = new Data.Builder()
                .putLong("sampleId", sampleId)
                .build();

        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(LumbarExtensionTrainingUploader.class)
                .setInputData(data)
                .setConstraints(constraints)
                .build();

        workManager.enqueueUniqueWork("smart4health_pdf_" + sampleId, ExistingWorkPolicy.APPEND_OR_REPLACE, workRequest);
    }

    private void addPeriodicWork(Class<? extends ListenableWorker> worker, String name, int time, TimeUnit timeUnit) {
        final Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        final PeriodicWorkRequest.Builder periodicWorkBuilder = new PeriodicWorkRequest.Builder(worker, time, timeUnit);
        periodicWorkBuilder.setConstraints(constraints);

        final PeriodicWorkRequest periodicWork = periodicWorkBuilder.build();

        workManager.enqueueUniquePeriodicWork(name,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWork);
    }

    private void cancelWork(String workName) {
        //workManager.cancelAllWorkByTag(workName);
        workManager.cancelUniqueWork(workName);
    }

    public WorkInfo.State getWorkers(@NonNull Context context, String workName) throws ExecutionException, InterruptedException {
        //to check if work is running:
        //there always only one, if it's running, we should get a state
        if (WorkManager.getInstance(context).getWorkInfosForUniqueWork(workName)
                .get().size() < 1) {
            System.out.println("No instances running.");
            //work is not running -> is WorkInfo.State.CANCELLED or WorkInfo.State.FAILED or WorkInfo.State.BLOCKED
            return WorkManager.getInstance(context)
                    .getWorkInfosForUniqueWork(workName).get().get(0).getState();
        } else {
            System.out.println(WorkManager.getInstance(context)
                    .getWorkInfosForUniqueWork(workName).get().get(0).getState());
            return WorkManager.getInstance(context)
                    .getWorkInfosForUniqueWork(workName).get().get(0).getState();
            //returns WorkInfo.State.ENQUEUED if scheduled, WorkInfo.State.RUNNING if running
        }
    }
}