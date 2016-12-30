package com.example.ayou7995.facelock;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

public class PasswordVerificationFragment extends Fragment {

    private final static String TAG = "PasswordVerifyFragment";
    // private final static String TAG = "Jonathan";

    private static final String faceImgName = "FaceLock";
    private static final File mediaStorageDir =
            new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "MyCameraApp");
    private static File mediaFile = null;

    private EditText username_et;
    private EditText password_et;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.password_verifiaction_fragment, container, false);
        username_et = (EditText) view.findViewById(R.id.verify_name);
        password_et = (EditText) view.findViewById(R.id.verify_password);
        Button confirm_btn = (Button) view.findViewById(R.id.passVerify_button);
        Button faceVerify_btn = (Button) view.findViewById(R.id.faceVerify_button);

        assert ((MainActivity)getActivity()).getActionState().equals(MainActivity.VERIFYSTATE);

        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkEmpty()) {
                    login();
                }
            }
        });
        faceVerify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setCurrentFragment(MainActivity.PHOTOFRAG);
                ((MainActivity) getActivity()).replaceFragments(PhotoFragment.class);
            }
        });
        return view;
    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        if (getFragmentManager().findFragmentById(this.getId()) != null) {
//            Log.i(TAG, TAG + " destroy.");
//            getFragmentManager().beginTransaction().remove(this)
//                    .commit();
//        }
//    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private boolean checkEmpty() {

        String username = username_et.getText().toString();
        String password = password_et.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(),"Inputs cannot be empty.", Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Username or password is empty.");
            return true;
        }
        return false;
    }

    private void login() {

        Log.d(TAG,"Login");

        String username = username_et.getText().toString();
        String password = password_et.getText().toString();

        // Todo
        // request for verification by password
        // if true : return success, username, password, filepath
        // if false : return fail

        final ProgressDialog dialog = ProgressDialog.show(getActivity(),
                "Verifying", "It might take a few seconds...",true);
        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    Thread.sleep(3000);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                finally{
                    dialog.dismiss();
                }
            }
        }).start();

        String valid = "success";
        if(valid.equals("success")) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + faceImgName + ".jpg");
            ((MainActivity) getActivity()).setUser("ayou7995");
            ((MainActivity) getActivity()).setPass("password");
            ((MainActivity) getActivity()).setFile(mediaFile);

            ((MainActivity) getActivity()).setActionState(MainActivity.IDLESTATE);
            ((MainActivity) getActivity()).setCurrentFragment(MainActivity.LOBBYFRAG);
            ((MainActivity) getActivity()).replaceFragments(LobbyFragment.class);

        }
        else if (valid.equals("fail")){
            Toast.makeText(getActivity(), "Not yet register ?",Toast.LENGTH_SHORT).show();
        }
    }
}
