package com.example.ayou7995.facelock;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
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

import static android.content.Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP;
import static android.provider.Settings.Secure.ANDROID_ID;

public class MainActivity extends FragmentActivity {

    private final static String TAG = "Jonathan";

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

    // User Detail
    private String currentUser = "";
    private String currentPass = "";
    // TODO: add currentDeviceID
    private String currentDeviceID = Settings.Secure.ANDROID_ID;
    private String binaryData = "";
    private File currentFile = null;
    private BootBroadcastReceiver bootBroadcastReceiver;

    Boolean profileExists = true;

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

        FragmentManager fragmentManager = getFragmentManager();
        LobbyFragment lobbyFragment = new LobbyFragment();
        PhotoFragment photoFragment = new PhotoFragment();

        // Todo
        // Check if any profile exists
        currentActionState = CHECKEXIST;
        checkExistence sender = new checkExistence();
        sender.execute(createInfoJSON());

        if (profileExists) {
            currentFragment = PHOTOFRAG;
            currentActionState = VERIFYSTATE;
            fragmentManager.beginTransaction().add(R.id.main_frameLayout, photoFragment).commit();
        } else {
            currentFragment = LOBBYFRAG;
            currentActionState = IDLESTATE;
            fragmentManager.beginTransaction().add(R.id.main_frameLayout, lobbyFragment).commit();
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

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    private class checkExistence extends AsyncTask<JSONObject, Void, String> {

        @Override
        protected String doInBackground(JSONObject... params) {
            String url = "http://163.28.17.73:8000/";
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

            } catch (JSONException e) {
                System.out.println("unable to catch response\n");
            }
        }
    }
}