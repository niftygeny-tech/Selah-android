package com.selah.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

public final class NotificationHelper {
    public static final String CHANNEL_ID = "selah_reminders";
    private NotificationHelper() {}

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        Uri sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + com.selah.app.R.raw.dove_chime);
        AudioAttributes attrs = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID, "Selah reminders", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Scheduled Selah reminders");
        channel.setSound(sound, attrs);
        channel.enableVibration(true);
        nm.createNotificationChannel(channel);
    }
}
