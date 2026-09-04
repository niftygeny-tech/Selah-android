package com.selah.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import java.util.Random;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String[] LINES = {
        "Selah. Your verse is waiting",
        "Mutter it once more",
        "Day or night — it hasn't left your mouth",
        "The Book won't depart. Will you?"
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper.createChannel(context);

        android.content.SharedPreferences p =
            context.getSharedPreferences("selah_reminders", Context.MODE_PRIVATE);
        String text = p.getString("text", "");
        if (TextUtils.isEmpty(text)) return;

        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent tap = PendingIntent.getActivity(
            context, 8702, open,
            PendingIntent.FLAG_UPDATE_CURRENT |
                (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));

        String line = LINES[new Random().nextInt(LINES.length)];
        android.app.Notification.Builder b;
        if (Build.VERSION.SDK_INT >= 26) {
            b = new android.app.Notification.Builder(context, NotificationHelper.CHANNEL_ID);
        } else {
            b = new android.app.Notification.Builder(context)
                .setSound(android.net.Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.dove_chime));
        }

        b.setSmallIcon(R.drawable.ic_selah_notification)
            .setContentTitle(line)
            .setContentText(text.length() > 160 ? text.substring(0, 160) : text)
            .setStyle(new android.app.Notification.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(tap)
            .setCategory(android.app.Notification.CATEGORY_REMINDER)
            .setPriority(android.app.Notification.PRIORITY_HIGH)
            .setWhen(System.currentTimeMillis());

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify((int)(System.currentTimeMillis() / 1000L), b.build());

        // Schedule the next reminder without needing the app UI to be open.
        ReminderScheduler.scheduleNext(context);
    }
}
