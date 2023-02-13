package pt.uninova.s4h.citizenhub.wearbasic.message;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import androidx.lifecycle.MutableLiveData;

public class MessageService extends WearableListenerService {

    public static MutableLiveData<String> connection = new MutableLiveData<>();
    public static MutableLiveData<String> heartRate = new MutableLiveData<>();
    public static MutableLiveData<String> steps = new MutableLiveData<>();
    public static MutableLiveData<String> agent = new MutableLiveData<>();

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String citizenHubPath = "/citizenhub_";
        String phoneConnectedPath = "connection";
        String heartRatePath = "heartrate";
        String stepsPath = "steps";
        String agentPath = "agent";

        if(messageEvent.getPath().equals(citizenHubPath + phoneConnectedPath))
        {
            final String message = new String(messageEvent.getData());
            connection.postValue(message);
        }
        else if (messageEvent.getPath().equals(citizenHubPath + heartRatePath))
        {
            final String message = new String(messageEvent.getData());
            heartRate.postValue(message);
        }
        else if (messageEvent.getPath().equals(citizenHubPath + stepsPath))
        {
            final String message = new String(messageEvent.getData());
            steps.postValue(message);
        }
        else if (messageEvent.getPath().equals(citizenHubPath + agentPath))
        {
            final String message = new String(messageEvent.getData());
            agent.postValue(message);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }
}
