package pt.uninova.s4h.citizenhub.wearbasic.work;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.data.Device;
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
            System.out.println("Sync Worker is doing work.");
            sendSteps();
            sendHeartRate();
            return Result.success();
        }
        catch (Throwable throwable)
        {
            System.out.println("Sync Worker failed to do work.");
            return Result.failure();
        }
    }



    private void sendSteps(){
        //TODO get steps value from local db
        int steps = 0;
        System.out.println("Sending Steps value to phone: " + steps);
        new SendMessage(getApplicationContext().getString(R.string.citizen_hub_path) + nodeIdString, steps + "," + new Date().getTime() + "," + StepsSnapshotMeasurement.TYPE_STEPS_SNAPSHOT).start();
    }

    private void sendHeartRate(){
        //TODO get heart rate value from local db
        int heartRate = 0;
        System.out.println("Sending HR value to phone: " + heartRate);
        new SendMessage(getApplicationContext().getString(R.string.citizen_hub_path) + nodeIdString, heartRate + "," + new Date().getTime() + "," + HeartRateMeasurement.TYPE_HEART_RATE).start();
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
                System.out.println("Node associated: " + n.getId() + " Message: " + message);
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