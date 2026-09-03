package com.selah.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public final class ReminderScheduler {
    private static final String PREFS = "selah_reminders";
    private static final String KEY_TEXT = "text";
    private static final String KEY_FREQ = "freq";
    private static final String KEY_START = "start";
    private static final String KEY_END = "end";
    private static final int REQUEST_CODE = 8701;

    private ReminderScheduler() {}

    public static void saveAndSchedule(Context context, String text, int frequency, String start, String end) {
        android.content.SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        p.edit().putString(KEY_TEXT, text == null ? "" : text.trim())
            .putInt(KEY_FREQ, Math.max(10, frequency))
            .putString(KEY_START, start == null ? "07:00" : start)
            .putString(KEY_END, end == null ? "22:00" : end)
            .apply();
        scheduleNext(context);
    }

    public static void scheduleNext(Context context) {
        cancel(context);
        android.content.SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String text = p.getString(KEY_TEXT, "");
        if (text == null || text.trim().isEmpty()) return;

        Calendar next = findNext(
            p.getInt(KEY_FREQ, 120),
            p.getString(KEY_START, "07:00"),
            p.getString(KEY_END, "22:00")
        );
        if (next == null) return;

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        PendingIntent pi = pendingIntent(context);
        long when = next.getTimeInMillis();

        if (Build.VERSION.SDK_INT >= 31 && am.canScheduleExactAlarms()) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
        } else if (Build.VERSION.SDK_INT >= 23) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, when, pi);
        }
    }

    public static void cancel(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(pendingIntent(context));
    }

    private static PendingIntent pendingIntent(Context context) {
        Intent i = new Intent(context, ReminderReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(context, REQUEST_CODE, i, flags);
    }

    private static int[] hm(String value) {
        try {
            String[] a = value.split(":");
            return new int[]{Integer.parseInt(a[0]), Integer.parseInt(a[1])};
        } catch (Exception e) {
            return new int[]{7, 0};
        }
    }

    public static Calendar findNext(int frequency, String start, String end) {
        int[] st = hm(start);
        int[] en = hm(end);
        frequency = Math.max(10, frequency);

        Calendar now = Calendar.getInstance();
        Calendar candidate = Calendar.getInstance();
        candidate.set(Calendar.SECOND, 0);
        candidate.set(Calendar.MILLISECOND, 0);

        for (int day = 0; day < 3; day++) {
            candidate.setTimeInMillis(now.getTimeInMillis());
            candidate.add(Calendar.DAY_OF_YEAR, day);
            candidate.set(Calendar.HOUR_OF_DAY, st[0]);
            candidate.set(Calendar.MINUTE, st[1]);
            candidate.set(Calendar.SECOND, 0);
            candidate.set(Calendar.MILLISECOND, 0);

            int endMin = en[0] * 60 + en[1];
            int startMin = st[0] * 60 + st[1];
            int currentMin = (day == 0) ? now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE) : -1;

            for (int offset = 0; offset <= 24 * 60; offset += frequency) {
                int total = startMin + offset;
                Calendar c = (Calendar) candidate.clone();
                c.add(Calendar.MINUTE, offset);
                int minuteOfDay = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
                boolean within = endMin >= startMin
                    ? minuteOfDay <= endMin
                    : (minuteOfDay >= startMin || minuteOfDay <= endMin);

                if (within && c.after(now)) return c;
                if (endMin >= startMin && total > endMin) break;
            }
        }
        return null;
    }
}
