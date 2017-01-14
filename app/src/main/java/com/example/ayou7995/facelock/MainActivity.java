package com.example.ayou7995.facelock;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.example.ayou7995.facelock.utils.LockscreenService;
import com.example.ayou7995.facelock.utils.LockscreenUtils;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends FragmentActivity
        implements LockscreenUtils.OnLockStatusChangedListener{

    private final static String TAG = "Jonathan";
    private final static String tag = "[MainActivity] : ";

    // Fragment Control
    private int currentFragment = 0;
    public static int LOBBYFRAG = 1;
    public static int REGISTERFRAG = 2;
    public static int PHOTOFRAG = 3;
    public static int PASSVERFRAG = 4;

    // State Control
    private String currentActionState = "";
    public static String IDLESTATE = "idle";
    public static String CHECKEXIST = "exist";
    public static String REGISTERSTATE = "register";
    public static String VERIFYSTATE = "verify";
    public static String PASSWORDSTATE = "login_passwd";
    public static String UPDATESTATE = "update";

    private String launchBy = "";
    public static String APPLAUNCH = "applanch";
    public static String SCREEENON = "screenon";

    // User Detail
    private String currentUser = "";
    private String currentPass = "";
    // TODO: add currentDeviceID
    private String currentDeviceID = "";
    // private String currentDeviceID = Settings.Secure.ANDROID_ID;
    private String binaryData = "";
    private File currentFile = null;
    private BootBroadcastReceiver bootBroadcastReceiver;

    private FragmentManager fragmentManager;
    PhotoFragment photoFragment;
    LobbyFragment lobbyFragment;

    Boolean profileExists = true;

    private LockscreenUtils mLockscreenUtils;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        fragmentManager = getFragmentManager();
        lobbyFragment = new LobbyFragment();
        photoFragment = new PhotoFragment();

        currentDeviceID = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

        mLockscreenUtils = new LockscreenUtils();

        launchBy = APPLAUNCH;

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle!=null) {
            Log.i(TAG,tag+"++++++++++++++++inside bundle+++++++++++");

            if(bundle.containsKey("launchBy")) {
                Log.i(TAG,tag+"++++++++++++++++inside containsKey+++++++++++");
                launchBy = bundle.getString("launchBy", APPLAUNCH);
                Log.i(TAG, tag + "====== launchBy : " + launchBy + " ======");
            }
        }

        currentActionState = CHECKEXIST;
        checkExistence sender = new checkExistence();
        sender.execute(createInfoJSON());

        // unlock screen in case of app get killed by system
        if (getIntent() != null && getIntent().hasExtra("kill")
                && getIntent().getExtras().getInt("kill") == 1) {
            // enableKeyguard();
            unlockHomeButton();
            Log.i(TAG,tag+"EnableKeyguard.");
        } else {

            try {
                Log.i(TAG,tag+"inside try.");
                // disable keyguard
                // disableKeyguard();

                // lock home button
                lockHomeButton();

                // start service for observing intents
                startService(new Intent(this, LockscreenService.class));

                // listen the events get fired during the call
                MainActivity.StateListener phoneStateListener = new MainActivity.StateListener();
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                telephonyManager.listen(phoneStateListener,
                        PhoneStateListener.LISTEN_CALL_STATE);

            } catch (Exception e) {
                Log.i(TAG,tag + e.toString());
            }

        }

        // Broadcast for detecting screen status
        bootBroadcastReceiver = new BootBroadcastReceiver();
        IntentFilter bootFilter = new IntentFilter();
        bootFilter.addAction(Intent.ACTION_SCREEN_ON);
        bootFilter.addAction(Intent.ACTION_SCREEN_OFF);
        bootFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(bootBroadcastReceiver, bootFilter);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }



    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bootBroadcastReceiver);
    }

    public void replaceFragments(Class fragmentClass) {
        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!isFinishing()) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_frameLayout, fragment).commit();
        }
    }

    public void setCurrentFragment(int frag) {
        currentFragment = frag;
    }

    public String getActionState() {
        return currentActionState;
    }
    public void setActionState(String actionState) {
        currentActionState = actionState;
    }

    public String getUser() { return currentUser; }
    public void setUser(String user) {
        currentUser = user;
    }

    public String getPass() {
        return currentPass;
    }
    public void setPass(String pass) {
        currentPass = pass;
    }

    public String getCurrentDeviceID() { return currentDeviceID; }
    public  void setCurrentDeviceID(String deviceID) { currentDeviceID = deviceID; }

    public String getBinaryData() { return binaryData; }
    public void setBinaryData(String data) { binaryData = data; }

    public File getFile() {
        return currentFile;
    }
    public void setFile(File file) {
        currentFile = file;
    }

    public String getLaunchBy() { return launchBy; }
    public void setLaunchBy(String launch) { launchBy = launch; }


    public JSONObject createInfoJSON() {
        JSONObject sendInfo = new JSONObject();
        try {
            sendInfo.put("status", currentActionState);
            sendInfo.put("ID", currentDeviceID);
            sendInfo.put("face", binaryData);
            sendInfo.put("name", currentUser);
            sendInfo.put("passwd", currentPass);
        } catch (JSONException e) {
            System.out.println("CREATE JSON FAIL");
        }

        return sendInfo;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();
        unlockHomeButton();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    private class checkExistence extends AsyncTask<JSONObject, Void, String> {

        @Override
        protected String doInBackground(JSONObject... params) {
            String url = "http://163.28.17.73:8000/server/";
            URL object;
            HttpURLConnection con;
            ConnectivityManager ConnMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = ConnMgr.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected()) {
                System.out.println("Device no Connection!\n");
            }
            try {
                object = new URL(url);
                con = (HttpURLConnection) object.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestMethod("POST");
                con.connect();
                for (JSONObject item : params) {
                    OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
                    wr.write(item.toString());
                    wr.flush();
                    wr.close();
                }
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try {
                        BufferedReader input = new BufferedReader(
                                new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuilder result = new StringBuilder();
                        while ((inputLine = input.readLine()) != null) {
                            result.append(inputLine);
                        }
                        input.close();
                        Log.d(TAG,result.toString());
                        return result.toString();
                    } catch (IOException e) {
                        System.out.println("no response!\n");
                    }
                } else {
                    System.out.println(con.getResponseMessage());
                    System.out.println("connection failed\n");
                }
            } catch (MalformedURLException e) {
                System.out.println("Invalid URL!");
                return null;
            } catch (IOException e) {
                System.out.println("Fail to connect!");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                System.out.println("no result!\n");
                return;
            }
            JSONObject returnInformation;
            try {
                returnInformation = new JSONObject(result);

                profileExists = (boolean) returnInformation.get("exist");
//                profileExists = false;
                while(getSupportFragmentManager().getFragments()!=null) {
                    Log.i(TAG,tag+"Fragment pop back.");
                    getSupportFragmentManager().popBackStack();
                }
                if(!isFinishing()) {
                    if (profileExists) {
                        currentFragment = PHOTOFRAG;
                        currentActionState = VERIFYSTATE;
                        fragmentManager.beginTransaction().add(R.id.main_frameLayout, photoFragment).commitAllowingStateLoss();
                    } else {
                        currentFragment = LOBBYFRAG;
                        currentActionState = IDLESTATE;
                        fragmentManager.beginTransaction().add(R.id.main_frameLayout, lobbyFragment).commitAllowingStateLoss();
                    }
                }

            } catch (JSONException e) {
                System.out.println("unable to catch response\n");
            }
        }
    }

    // Handle events of calls and unlock screen if necessary
    private class StateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    unlockHomeButton();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    };

    // Don't finish Activity on Back press
    // @Override
    // public void onBackPressed() {return;}


    // Handle button clicks
//    @Override
//    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
//
//        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
//                || (keyCode == KeyEvent.KEYCODE_POWER)
//                || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
//                || (keyCode == KeyEvent.KEYCODE_CAMERA)) {
//            return true;
//        }
//        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
//
//            return true;
//        }
//
//        return false;
//
//    }

    // handle the key press events here itself
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
//                || (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
//                || (event.getKeyCode() == KeyEvent.KEYCODE_POWER)) {
//            return false;
//        }
//        if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {
//
//            return true;
//        }
//        return false;
//    }

    // Lock home button
    public void lockHomeButton() {
        mLockscreenUtils.lock(MainActivity.this);
    }

    // Unlock home button and wait for its callback
    public void unlockHomeButton() {
        mLockscreenUtils.unlock();
    }

    // Simply unlock device when home button is successfully unlocked
    @Override
    public void onLockStatusChanged(boolean isLocked) {
        if (!isLocked) {
            unlockDevice();
        }
    }

//    @SuppressWarnings("deprecation")
//    private void disableKeyguard() {
//        KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
//        KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
//        mKL.disableKeyguard();
//    }
//
//    @SuppressWarnings("deprecation")
//    private void enableKeyguard() {
//        KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
//        KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
//        mKL.reenableKeyguard();
//    }

    //Simply unlock device by finishing the activity
    public void unlockDevice()
    {
        // android.os.Process.killProcess(android.os.Process.myPid());
        finish();
    }
}