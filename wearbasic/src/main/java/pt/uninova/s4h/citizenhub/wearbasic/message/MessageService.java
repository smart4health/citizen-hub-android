package pt.uninova.s4h.citizenhub.wearbasic.message;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import androidx.lifecycle.MutableLiveData;

public class MessageService extends WearableListenerService {

    public static MutableLiveData<String> heartRate = new MutableLiveData<>();
    public static MutableLiveData<String> steps = new MutableLiveData<>();

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String citizenHubPath = "/citizenhub_";
        String heartRatePath = "heartrate";
        String stepsPath = "steps";

        if (messageEvent.getPath().equals(citizenHubPath + heartRatePath))
        {
            final String message = new String(messageEvent.getData());
            heartRate.postValue(message);
        }
        else if (messageEvent.getPath().equals(citizenHubPath + stepsPath))
        {
            final String message = new String(messageEvent.getData());
            steps.postValue(message);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }
}
