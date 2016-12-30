package com.example.ayou7995.facelock;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;

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
    public static String IDLESTATE = "Idle";
    public static String REGISTERSTATE = "Register";
    public static String VERIFYSTATE = "Verify";
    public static String UPDATESTATE = "Update";

    // User Detail
    private String currentUser = "";
    private String currentPass = "";
    private File currentFile = null;

    private BootBroadcastReceiver bootBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getFragmentManager();
        LobbyFragment lobbyFragment = new LobbyFragment();
        PhotoFragment photoFragment = new PhotoFragment();

        // Todo
        // Check if any profile exists
        Boolean profileExists = true;

        if(profileExists){
            currentFragment = PHOTOFRAG;
            currentActionState = VERIFYSTATE;
            fragmentManager.beginTransaction().add(R.id.main_frameLayout, photoFragment).commit();
        }
        else {
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
        if(!isFinishing()) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_frameLayout, fragment).commit();
        }
    }

    public void setCurrentFragment(int frag) { currentFragment = frag; }
    public String getActionState() { return currentActionState; }
    public void setActionState(String actionState) { currentActionState = actionState; }
    public String getUser() { return currentUser; }
    public void setUser(String user) { currentUser = user; }
    public String getPass() { return currentPass; }
    public void setPass(String pass) { currentPass = pass; }
    public File getFile() { return currentFile; }
    public void setFile(File file) { currentFile = file; }
}