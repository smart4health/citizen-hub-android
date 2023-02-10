package pt.uninova.s4h.citizenhub.wearbasic.message;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import androidx.lifecycle.MutableLiveData;

public class MessageService extends WearableListenerService {

    public static MutableLiveData<String> phoneConnected = new MutableLiveData<>();

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String citizenHubPath = "/citizenhub_";
        String phoneConnectedPath = "WearOSConnected";

        if(messageEvent.getPath().equals(citizenHubPath + phoneConnectedPath))
        {
            final String message = new String(messageEvent.getData());
            phoneConnected.postValue(message);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }
}
