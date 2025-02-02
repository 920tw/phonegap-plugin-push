package com.adobe.phonegap.push;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class PushHandlerActivity extends Activity implements PushConstants {
    private static String LOG_TAG = "PushPlugin_PushHandlerActivity";

    /*
     * this activity will be started if the user touches a notification that we own.
     * We send it's data off to the push plugin for processing.
     * If needed, we boot up the main activity to kickstart the application.
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        GCMIntentService gcm = new GCMIntentService();
        int notId = getIntent().getExtras().getInt(NOT_ID, 0);
        Log.d(LOG_TAG, "not id = " + notId);
        gcm.setNotification(notId, "");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(GCMIntentService.getAppName(this), notId);
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate");
        String callback = getIntent().getExtras().getString("callback");
        Log.d(LOG_TAG, "callback = " + callback);
        boolean foreground = getIntent().getExtras().getBoolean("foreground", true);

        Log.d(LOG_TAG, "bringToForeground = " + foreground);

        boolean isPushPluginActive = PushPlugin.isActive();
        processPushBundle(isPushPluginActive);

        finish();

        Log.d(LOG_TAG, "isPushPluginActive = " + isPushPluginActive);

        if (!isPushPluginActive && foreground) {
            Log.d(LOG_TAG, "forceMainActivityReload");
            forceMainActivityReload();
        } else {
            Log.d(LOG_TAG, "don't want main activity");
        }
    }

    /**
     * Takes the pushBundle extras from the intent,
     * and sends it through to the PushPlugin for processing.
     */
    private void processPushBundle(boolean isPushPluginActive) {
        Bundle extras = getIntent().getExtras();

        if (extras != null && !PushPlugin.isIntercomPush(extras))	{
            Bundle originalExtras = extras.getBundle(PUSH_BUNDLE);

            originalExtras.putBoolean(FOREGROUND, false);
            originalExtras.putBoolean(COLDSTART, !isPushPluginActive);
            originalExtras.putString(ACTION_CALLBACK, extras.getString(CALLBACK));

            PushPlugin.sendExtras(originalExtras);
        }
    }

    /**
     * Forces the main activity to re-launch if it's unloaded.
     */
    private void forceMainActivityReload() {
        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
        startActivity(launchIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
}
