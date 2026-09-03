# Selah Android — standalone reminder app

Selah is packaged as a standalone Android app. The existing web UI is bundled locally inside the APK, so the app does not need to open a browser or depend on the Selah website for its interface.

Native Android reminder handling is included:
- Android AlarmManager scheduling
- notification-channel sound bundled in the APK
- Android 13+ notification permission handling
- rescheduling after reboot/update
- local WebView with no browser address bar

## No Android Studio required

A GitHub Actions workflow is included at `.github/workflows/build-apk.yml`. It builds the debug APK in the cloud using GitHub Actions.

See `README-CLOUD.md` for the beginner-friendly steps.

## Reminder behavior

The web page is only the user interface. Android is responsible for scheduling and displaying reminder notifications, so reminders can fire while Selah is in the background or closed.

For testing, create a reminder a few minutes ahead, close Selah, lock the phone, and wait for the notification.
