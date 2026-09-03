# Selah — build the APK without Android Studio

This project includes a GitHub Actions workflow that builds the Android APK in the cloud. You do **not** need Android Studio or the Android SDK on your computer.

## Simple steps

1. Create/sign in to GitHub.
2. Create a new repository. A name such as `selah-android` is fine.
3. Upload the **contents of this SelahAndroid folder** to the repository (including `.github/workflows/build-apk.yml`).
4. Open the repository's **Actions** tab.
5. Select **Build Selah APK**.
6. Click **Run workflow** if it has not already started.
7. Wait for the green check mark.
8. Open the completed workflow run. Under **Artifacts**, download **Selah-debug-apk**.
9. Unzip the downloaded artifact. Inside is `app-debug.apk`.
10. Send `app-debug.apk` to the Android phone and install it.

### Important

- This is a **debug/test APK**. It is suitable for installing on your phone and testing the reminder system.
- Android may ask you to allow notifications.
- If Selah asks for exact-alarm access, allow it so scheduled reminders can be as precise as Android permits.
- Test with a reminder a few minutes in the future, then close Selah and lock the phone.
- Some phone manufacturers have additional battery-saving restrictions. If reminders are delayed on your phone, tell me the phone brand/model and I can guide you through the relevant setting.
