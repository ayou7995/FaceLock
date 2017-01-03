package com.example.ayou7995.facelock;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RegisterFragment extends Fragment {

    private final static String TAG = "Isabella";

    private EditText name_et;
    private EditText pass_et;
    private ImageView face_img;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_fragment, container, false);

        // Initialize widgets
        face_img = (ImageView) view.findViewById(R.id.face_image);
        Button recaptureB = (Button) view.findViewById(R.id.recapture_button);
        Button registerB = (Button) view.findViewById(R.id.register_button);
        name_et = (EditText) view.findViewById(R.id.input_name);
        pass_et = (EditText) view.findViewById(R.id.input_password);

        if(((MainActivity)getActivity()).getActionState().equals(MainActivity.REGISTERSTATE)) {
            registerB.setText(R.string.register);
        }
        else if(((MainActivity)getActivity()).getActionState().equals(MainActivity.UPDATESTATE)) {
            registerB.setText(R.string.update);
        }

        displayInformation();

        registerB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
                Toast.makeText(getActivity(),"Verification Fail",Toast.LENGTH_SHORT).show();
                ((MainActivity)getActivity()).setActionState(MainActivity.IDLESTATE);
                ((MainActivity)getActivity()).setCurrentFragment(MainActivity.LOBBYFRAG);
                ((MainActivity)getActivity()).replaceFragments(LobbyFragment.class);
            }
        });
        recaptureB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).setCurrentFragment(MainActivity.PHOTOFRAG);
                ((MainActivity)getActivity()).replaceFragments(PhotoFragment.class);
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

    private void register() {

        Log.i(TAG,"Register");

        // Todo
        // Register profile to backend
//        final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
//                R.style.AppTheme);
//        progressDialog.setIndeterminate(true);
//        progressDialog.setMessage("Register...");
//        progressDialog.show();
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

        /** send register info to server **/
        String name = name_et.getText().toString();
        String password = pass_et.getText().toString();

        if (!name.isEmpty() && !password.isEmpty()) {
            ((MainActivity) getActivity()).setUser(name);
            ((MainActivity) getActivity()).setPass(password);
            registerSender sender = new registerSender();
            sender.execute(((MainActivity) getActivity()).createInfoJSON());
        }
    }

    private void showFace(File f) {

        try {
            ExifInterface exif = new ExifInterface(f.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            int angle = 0;

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                Log.v(TAG, "90 degrees rotation.");
                angle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                Log.v(TAG, "180 degrees rotation.");
                angle = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                Log.v(TAG, "270 degrees rotation.");
                angle = 270;
            }

            Matrix mat = new Matrix();
            mat.postRotate(angle);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;

            Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f),
                    null, options);
            Bitmap bitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                    bmp.getHeight(), mat, true);
            ByteArrayOutputStream outstudentstreamOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100,
                    outstudentstreamOutputStream);
            face_img.setImageBitmap(bitmap);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayInformation() {

        File pictureFile = ((MainActivity) getActivity()).getFile();

        Bitmap bitmap = null;
        if(pictureFile.exists()){
            Log.i(TAG, "Loading Img_FaceLock.jpg.");
            bitmap = BitmapFactory.decodeFile(pictureFile.toString());
        }
        else {
            Log.i(TAG, "Img_FaceLock.jpg doesn't exists.");
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        Bitmap finalBitmap = null;
        if (bitmap != null) {
            finalBitmap = Bitmap.createBitmap(bitmap , 0, 0,
                    bitmap .getWidth(), bitmap .getHeight(),
                    matrix, true);
        }

        face_img.setImageBitmap(finalBitmap);

        if(((MainActivity)getActivity()).getActionState().equals(MainActivity.REGISTERSTATE)){
            name_et.setText("");
            pass_et.setText("");
        }
        else if(((MainActivity)getActivity()).getActionState().equals(MainActivity.UPDATESTATE)) {
            name_et.setText(((MainActivity) getActivity()).getUser());
            pass_et.setText(((MainActivity) getActivity()).getPass());
        }
    }
    private class registerSender extends AsyncTask<JSONObject, Void, String> {

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
                success = (boolean) returnInformation.get("OK");

            } catch (JSONException e) {
                System.out.println("unable to catch response\n");
            }
            if (!success) {
                Toast.makeText(getActivity(),
                        "You have already registered.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
