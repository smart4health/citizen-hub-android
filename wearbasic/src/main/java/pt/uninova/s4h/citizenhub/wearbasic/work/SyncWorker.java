package pt.uninova.s4h.citizenhub.wearbasic.work;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.time.LocalDate;
import java.util.ArrayList;
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

    private String nodeIdString;

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
        final LocalDate now = LocalDate.now();

        MainActivity.tagRepository.selectBasedOnLabel(Tag.LABEL_MEASUREMENT_NOT_SYNCHRONIZED, values -> {
            System.out.println("IDs: " + values);
            for (Integer id : values)
            {
                // get steps record by id (make method and query)
                // send message
                // update label
            }

            // if (value != null)
            // new SendMessage(getApplicationContext().getString(R.string.citizen_hub_path) + nodeIdString, value + "," + new Date().getTime() + "," + StepsSnapshotMeasurement.TYPE_STEPS_SNAPSHOT).start();
            // tagRepository.updateLabel(sampleId, Tag.LABEL_MEASUREMENT_SYNCHRONIZED);
        });
    }

    private void sendHeartRate(){
        final LocalDate now = LocalDate.now();
        //TODO
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
                Task<Node> t = Wearable.getNodeClient(getApplicationContext()).getLocalNode();
                Node n = Tasks.await(t);
                nodeIdString = n.getId();
                List<Node> nodes = Tasks.await(nodeListTask);
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask = Wearable.getMessageClient(getApplicationContext()).sendMessage(node.getId(), path, message.getBytes());
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