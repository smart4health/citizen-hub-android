package pt.uninova.s4h.citizenhub.wearbasic.message;


import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MessageService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String citizenHubPath = "/citizenhub_";
        String phoneConnected = "WearOSConnected";

        if(messageEvent.getPath().equals(citizenHubPath + phoneConnected))
        {
            final String message = new String(messageEvent.getData());
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra(phoneConnected, message);
            // TODO deprecated, replace
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }

        else {
            super.onMessageReceived(messageEvent);
        }
    }
}
