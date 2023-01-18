package pt.uninova.s4h.citizenhub.wearbasic;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MessageService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String citizenHubPath = "/citizenhub_";
        String protocolPathHeartRate = "WearOSHeartRateProtocol";
        String protocolPathSteps = "WearOSStepsProtocol";
        String agentPath = "WearOSAgent";
        String phoneConnected = "WearOSConnected";

        if(messageEvent.getPath().equals(citizenHubPath + protocolPathHeartRate))
        {
            final String message = new String(messageEvent.getData());
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra(protocolPathHeartRate, message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        if(messageEvent.getPath().equals(citizenHubPath + protocolPathSteps))
        {
            final String message = new String(messageEvent.getData());
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra(protocolPathSteps, message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        if(messageEvent.getPath().equals(citizenHubPath + agentPath))
        {
            final String message = new String(messageEvent.getData());
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra(agentPath, message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        if(messageEvent.getPath().equals(citizenHubPath + phoneConnected))
        {
            final String message = new String(messageEvent.getData());
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra(phoneConnected, message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }

        else {
            super.onMessageReceived(messageEvent);
        }
    }
}
