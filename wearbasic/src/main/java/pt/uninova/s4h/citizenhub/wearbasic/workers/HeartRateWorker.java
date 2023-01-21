package pt.uninova.s4h.citizenhub.wearbasic.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class HeartRateWorker extends Worker {
    public HeartRateWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try{
            System.out.println("HeartRate Worker is doing work.");
            return Result.success();
        }
        catch (Throwable throwable)
        {
            System.out.println("HeartRate Worker failed to do work.");
            return Result.failure();
        }
    }
}
