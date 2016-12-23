package com.example.ayou7995.facelock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootBroadcastReceiver extends BroadcastReceiver {

    private final static String Tag = "Jonathan";

    @Override
    public void onReceive(Context context, Intent intent) {


        Log.i(Tag, "fuck that shit");
        if (intent != null && intent.getAction() != null)
        {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
            {
                // Screen is on but not unlocked (if any locking mechanism present)
                Log.i(Tag, "Action screen on.");
                Toast.makeText(context,"Action screen on.",Toast.LENGTH_SHORT).show();
                Intent launchLockIntent = new Intent(context,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchLockIntent);

            }
            else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
            {
                // Screen is locked
                Log.i(Tag, "Action screen off.");
                Toast.makeText(context,"Action screen off.",Toast.LENGTH_SHORT).show();
            }
            else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
            {
                // Screen is unlocked
                Log.i(Tag, "Action user present.");
                Toast.makeText(context,"Action user present.",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
