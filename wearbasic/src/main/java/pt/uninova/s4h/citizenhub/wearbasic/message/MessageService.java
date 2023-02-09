package pt.uninova.s4h.citizenhub.wearbasic.message;


import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class MessageService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String citizenHubPath = "/citizenhub_";
    }
}
