package pt.uninova.s4h.citizenhub.wearbasic.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class StepsWorker extends Worker {

    public static MutableLiveData<Integer> steps = new MutableLiveData<>();

    public StepsWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try{
            System.out.println("Steps Worker is doing work.");


            steps.postValue(0); //TODO
            return Result.success();
        }
        catch (Throwable throwable)
        {
            System.out.println("Steps Worker failed to do work.");
            return Result.failure();
        }
    }

    private void setListener(){

    }
}
