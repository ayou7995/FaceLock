package com.example.ayou7995.facelock;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    private String valid = "success";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.password_verifiaction_fragment, container, false);
        username_et = (EditText) view.findViewById(R.id.verify_name);
        password_et = (EditText) view.findViewById(R.id.verify_password);
        Button confirm_btn = (Button) view.findViewById(R.id.passVerify_button);
        Button faceVerify_btn = (Button) view.findViewById(R.id.faceVerify_button);

        assert ((MainActivity)getActivity()).getActionState().equals(MainActivity.PASSWORDSTATE);

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
                ((MainActivity) getActivity()).setActionState(MainActivity.VERIFYSTATE);
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

        // TODO:
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

        verifySender sender = new verifySender();
        sender.execute(((MainActivity) getActivity()).createInfoJSON());

    }
    private class verifySender extends AsyncTask<JSONObject, Void, String> {

        @Override
        protected String doInBackground(JSONObject... params) {
            String url = "http://163.28.17.73:8000/";
            URL object;
            HttpURLConnection con;
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
            boolean success = false;
            if (result == null) {
                System.out.println("no result!\n");
                return;
            }
            JSONObject returnInformation;
            try {

                returnInformation = new JSONObject(result);
                success = (boolean) returnInformation.get("valid");
                String user = (String) returnInformation.get("name");
                String password = (String) returnInformation.get("passwd");

                if(success) {
                    mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                            "IMG_" + user + ".jpg");
                    ((MainActivity) getActivity()).setUser("ayou7995");
                    ((MainActivity) getActivity()).setPass("password");
                    ((MainActivity) getActivity()).setFile(mediaFile);

                    ((MainActivity) getActivity()).setActionState(MainActivity.IDLESTATE);
                    ((MainActivity) getActivity()).setCurrentFragment(MainActivity.LOBBYFRAG);
                    ((MainActivity) getActivity()).replaceFragments(LobbyFragment.class);

                }
                else {
                    Toast.makeText(getActivity(), "Not yet register ?",Toast.LENGTH_SHORT).show();
                }

                ((MainActivity) getActivity()).setUser(user);
                ((MainActivity) getActivity()).setPass(password);

            } catch (JSONException e) {
                System.out.println("unable to catch response\n");
            }
        }
    }
}
