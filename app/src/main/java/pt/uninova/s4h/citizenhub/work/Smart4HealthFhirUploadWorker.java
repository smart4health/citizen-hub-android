package pt.uninova.s4h.citizenhub.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

public class Smart4HealthFhirUploadWorker extends ListenableWorker {

    public Smart4HealthFhirUploadWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture((completer) -> {

            return this.toString();
        });
    }
}
