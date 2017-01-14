package com.example.ayou7995.facelock.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.ayou7995.facelock.LockScreenActivity;
import com.example.ayou7995.facelock.MainActivity;

public class LockscreenIntentReceiver extends BroadcastReceiver {

    private final static String TAG = "Jonathan";
    private final static String tag = "[LockscreenIntentReceiver] : ";

    // Handle actions and display Lockscreen
    @Override
    public void onReceive(Context context, Intent intent) {

        //intent.getAction().equals(Intent.ACTION_SCREEN_OFF
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)
                || intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            start_lockscreen(context);
        }

    }

    // Display lock screen
    private void start_lockscreen(Context context) {
        Intent mIntent = new Intent(context, MainActivity.class);
        Log.i(TAG,tag+"++++++++++++++++inside start_lockscreen+++++++++++");
        Bundle bundle = new Bundle();
        bundle.putString("launchBy", "screenon");
        mIntent.putExtras(bundle);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mIntent);
    }

}
