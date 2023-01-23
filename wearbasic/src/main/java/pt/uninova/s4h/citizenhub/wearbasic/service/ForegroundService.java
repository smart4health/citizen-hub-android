package pt.uninova.s4h.citizenhub.wearbasic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.wear.ongoing.OngoingActivity;
import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.wearbasic.MainActivity;

public class ForegroundService extends Service {

    public static final String CHANNEL_ID = "CitizenWear";

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");

        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_content_title))
                .setContentText(input)
                .setSmallIcon(R.drawable.img_logo_figure)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .clearActions();
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        OngoingActivity ongoingActivity =
                new OngoingActivity.Builder(getApplicationContext(), 1, builder)
                        .setAnimatedIcon(R.drawable.img_logo_figure)
                        .setTouchIntent(pendingIntent)
                        .setTitle(getString(R.string.app_name))
                        .setOngoingActivityId(1)
                        .build();

        ongoingActivity.apply(getApplicationContext());
        manager.notify(1, notification);
        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}