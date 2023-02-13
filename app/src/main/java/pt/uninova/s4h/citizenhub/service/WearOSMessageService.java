package pt.uninova.s4h.citizenhub.service;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import pt.uninova.s4h.citizenhub.connectivity.wearos.WearOSConnection;

public class WearOSMessageService extends FragmentActivity implements MessageClient.OnMessageReceivedListener {

    private String nodeIdString, mobileIDString;
    private final Map<String,WearOSConnection> connectionMap = new HashMap<>();
    String citizenHubPath = "/citizenhub_";

    String checkConnectionPath = "checkConnection";
    Context appContext;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Wearable.getMessageClient(appContext).removeListener(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        new GetConnectedNode("wear", appContext).start();
        String dataPath = citizenHubPath + nodeIdString;

        if(connectionMap.get(nodeIdString)!= null){
            WearOSConnection wearOSConnection = connectionMap.get(nodeIdString);

            if (messageEvent.getPath().equals(citizenHubPath+checkConnectionPath)) {
                new  GetConnectedNode("mobile", appContext).start();

            }else if(messageEvent.getPath().equals(dataPath)) {
                wearOSConnection.enable();
                String message = new String(messageEvent.getData());

                if (!message.equals("")) {
                    String[] newMessage = message.split(",");
                    wearOSConnection.onCharacteristicChanged(newMessage);
                }
            } else {
                Wearable.getMessageClient(appContext).removeListener(this);
            }
        }
    }

    public String getAddress(WearOSConnection connection){
        return connection.getAddress();
    }

    public void sendMessage (String path,String message){
        new SendMessage(citizenHubPath+path,message,appContext).start();
    }

    public WearOSConnection connect(String address, String name, CitizenHubService service) {
        appContext = service;

        Wearable.getMessageClient(service).addListener(this);
        WearOSConnection wearOSConnection = new WearOSConnection(address, name, service);
        connectionMap.put(address, wearOSConnection);

        MessageClient.OnMessageReceivedListener listener = messageEvent -> {
            String message = new String(messageEvent.getData());
            String[] messageParsed = message.split(",",3);
            Date time = new Date(Long.parseLong(messageParsed[1]));
            System.out.println("MeasurementValue: " + messageParsed[0] +
                    " | MeasurementTime: " + time +
                    " | MeasurementKind: " + Integer.parseInt(messageParsed[2]) +
                    " | from nodeID: " + messageEvent.getSourceNodeId() +
                    " | from path: " + messageEvent.getPath());
        };

        MessageClient client = Wearable.getMessageClient(service);
        client.addListener(listener);

        return wearOSConnection;
    }

    class SendMessage extends Thread {
        String path;
        String message;
        Context context;

        SendMessage(String p, String m, Context c) {
            path = p;
            message = m;
            context = c;
        }

        public void run() {
            System.out.println("Sending: " + message);
            Task<List<Node>> wearableList = Wearable.getNodeClient(context).getConnectedNodes();
            try {
                List<Node> nodes = Tasks.await(wearableList);
                for (Node node : nodes) {

                    if(connectionMap.containsKey(node.getId())) {
                        Task<Integer> sendMessageTask =
                                Wearable.getMessageClient(context).sendMessage(node.getId(), path, message.getBytes());
                        try {
                            Integer result = Tasks.await(sendMessageTask);
                        } catch (ExecutionException | InterruptedException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            } catch (ExecutionException | InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }

    class GetConnectedNode extends Thread {
        String type;
        Context context;
        GetConnectedNode(String t, Context c) {
            type=t;
            context = c;
        }
        public void run() {
            if(type.equals("mobile")){
                Task<Node> nodeTask = Wearable.getNodeClient(context).getLocalNode();
                try {
                    Node node = Tasks.await(nodeTask);
                    mobileIDString = node.getId();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }else if(type.equals("wear")){
                Task<List<Node>> nodeTaskList = Wearable.getNodeClient(context).getConnectedNodes();
                try {
                    List<Node> nodes = Tasks.await(nodeTaskList);
                    for (Node node : nodes) {
                        nodeIdString = node.getId();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}