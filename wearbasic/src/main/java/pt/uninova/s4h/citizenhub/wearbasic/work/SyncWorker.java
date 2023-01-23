package pt.uninova.s4h.citizenhub.wearbasic.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SyncWorker extends Worker {
    public SyncWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try{
            System.out.println("Sync Worker is doing work.");
            return Result.success();
        }
        catch (Throwable throwable)
        {
            System.out.println("Sync Worker failed to do work.");
            return Result.failure();
        }
    }
}
