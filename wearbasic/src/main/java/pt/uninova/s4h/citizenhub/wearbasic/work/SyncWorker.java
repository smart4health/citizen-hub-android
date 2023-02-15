package pt.uninova.s4h.citizenhub.wearbasic.work;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.data.Tag;
import pt.uninova.s4h.citizenhub.data.HeartRateMeasurement;
import pt.uninova.s4h.citizenhub.data.StepsSnapshotMeasurement;
import pt.uninova.s4h.citizenhub.wearbasic.MainActivity;

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
            sendSteps();
            sendHeartRate();
            return Result.success();
        }
        catch (Throwable throwable)
        {
            return Result.failure();
        }
    }

    private void sendSteps(){
        MainActivity.tagRepository.selectBasedOnLabel(Tag.LABEL_MEASUREMENT_NOT_SYNCHRONIZED, values -> {
            for (Integer sampleId : values)
            {
                MainActivity.stepsSnapshotMeasurementRepository.selectBasedOnId(Long.valueOf(sampleId), value -> {
                    if (value != null)
                    {
                        MainActivity.sampleRepository.selectTimestampBasedOnId(Long.valueOf(sampleId), time -> {
                            new SendMessage(getApplicationContext().getString(R.string.citizen_hub_path) + MainActivity.nodeIdString, value + "," + time + "," + StepsSnapshotMeasurement.TYPE_STEPS_SNAPSHOT + "," + sampleId).start();
                            MainActivity.tagRepository.updateLabel(Long.valueOf(sampleId), Tag.LABEL_MEASUREMENT_SYNCHRONIZED);
                        });
                    }
                });
            }
        });
    }

    private void sendHeartRate(){
        MainActivity.tagRepository.selectBasedOnLabel(Tag.LABEL_MEASUREMENT_NOT_SYNCHRONIZED, values -> {
            for (Integer sampleId : values) {
                MainActivity.heartRateMeasurementRepository.selectBasedOnId(Long.valueOf(sampleId), value -> {
                    if (value != null) {
                        MainActivity.sampleRepository.selectTimestampBasedOnId(Long.valueOf(sampleId), time -> {
                            new SendMessage(getApplicationContext().getString(R.string.citizen_hub_path) + MainActivity.nodeIdString, value + "," + time + "," + HeartRateMeasurement.TYPE_HEART_RATE + "," + sampleId).start();
                            MainActivity.tagRepository.updateLabel(Long.valueOf(sampleId), Tag.LABEL_MEASUREMENT_SYNCHRONIZED);
                        });
                    }
                });
            }
        });
    }

    class SendMessage extends Thread {
        String path;
        String message;

        SendMessage(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {
            Task<List<Node>> nodeListTask = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {
                List<Node> nodes = Tasks.await(nodeListTask);
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask = Wearable.getMessageClient(getApplicationContext()).sendMessage(node.getId(), path, message.getBytes());
                    System.out.println("Message sent: " + path + " | " + message);
                    try {
                        Tasks.await(sendMessageTask);
                    } catch (ExecutionException | InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }
            } catch (ExecutionException | InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }
}