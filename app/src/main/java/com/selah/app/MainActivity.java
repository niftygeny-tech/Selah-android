package com.selah.app;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class MainActivity extends Activity {
    private WebView webView;
    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;
    private static final int REQUEST_NOTIFICATIONS = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(makeWebView());

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.getDefault());
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });

        NotificationHelper.createChannel(this);
    }

    private WebView makeWebView() {
        webView = new WebView(this);
        webView.setBackgroundColor(0xFF1B1F52);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new AndroidBridge(this), "AndroidBridge");
        webView.loadUrl("file:///android_asset/index.html");
        return webView;
    }

    public class AndroidBridge {
        private final Context context;
        AndroidBridge(Context context) { this.context = context; }

        @JavascriptInterface
        public void requestNotifications() {
            runOnUiThread(() -> {
                if (Build.VERSION.SDK_INT >= 33 &&
                        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
                } else {
                    askForExactAlarmsIfNeeded();
                }
            });
        }

        @JavascriptInterface
        public void scheduleReminders(String text, int frequencyMinutes, String startTime, String endTime) {
            ReminderScheduler.saveAndSchedule(context, text, frequencyMinutes, startTime, endTime);
        }

        @JavascriptInterface
        public void scheduleCustomReminders(String text, String customTimes) {
            ReminderScheduler.saveCustomTimesAndSchedule(
                context,
                text == null ? "" : text,
                customTimes == null ? "" : customTimes
            );
        }

        @JavascriptInterface
        public void syncReminderRotation(String mode, String libraryJson) {
            ReminderScheduler.saveRotationSettings(
                context,
                mode == null ? "current" : mode,
                libraryJson == null ? "[]" : libraryJson
            );
        }

        @JavascriptInterface
        public void cancelReminders() {
            ReminderScheduler.cancel(context);
        }

        @JavascriptInterface
        public void openBatterySettings() {
            runOnUiThread(() -> MainActivity.this.openBatterySettings());
        }

        @JavascriptInterface
        public void speak(String text) {
            runOnUiThread(() -> {
                if (textToSpeech == null || !ttsReady || text == null || text.trim().isEmpty()) {
                    return;
                }

                textToSpeech.stop();
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "selah-read-aloud");
            });
        }

        @JavascriptInterface
        public void stopSpeaking() {
            runOnUiThread(() -> {
                if (textToSpeech != null) {
                    textToSpeech.stop();
                }
            });
        }
    }

    private void openBatterySettings() {
        try {
            Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName())
            );
            startActivity(intent);
        } catch (Exception ignored) {
            // App settings are not available on this device.
        }
    }

    private void askForExactAlarmsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 31) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                    .setTitle("Allow reliable reminders")
                    .setMessage("One Eight uses Android's exact alarm permission so your reminders can arrive at the time you choose, even when the app is closed.")
                    .setPositiveButton("Open settings", (d, w) -> {
                        try {
                            startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse("package:" + getPackageName())));
                        } catch (Exception ignored) {
                            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + getPackageName())));
                        }
                    })
                    .setNegativeButton("Later", null)
                    .show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATIONS) {
            askForExactAlarmsIfNeeded();
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
