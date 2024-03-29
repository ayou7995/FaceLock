package com.example.ayou7995.facelock;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PhotoFragment extends Fragment {

    private static final String TAG = "Jonathan";
    private static final String tag = "[PhotoFragment] : ";
    
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static final String faceImgName = "FaceLock";
//    private static final String rotateImg = "_Rotate";
    // private Static final String facImgName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    // To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.
    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.
    private static final File mediaStorageDir =
            new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
    private static File mediaFile = null;

    private FrameLayout camera_preview;

    private ProgressDialog dialog;

    private Camera mCamera = null;
    private CameraPreview mPreview = null;
    private Camera.PictureCallback mPicture;
    private int frontCameraId = -1;
    private String valid = "success";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.photo_fragment, container, false);

        // Initialize widgets
        Button captureB = (Button) view.findViewById(R.id.button_capture);
        Button passverB = (Button) view.findViewById(R.id.pass_verify_button);
        camera_preview = (FrameLayout) view.findViewById(R.id.camera_preview);

        frontCameraId = CameraHardware.getFrontCameraId();

        if( ((MainActivity)getActivity()).getActionState().equals(MainActivity.REGISTERSTATE) ||
            ((MainActivity)getActivity()).getActionState().equals(MainActivity.UPDATESTATE) ) {
            passverB.setVisibility(View.GONE);
        }

        captureB.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        Log.i(TAG,"Capture button clicked.");
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
        passverB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseCameraAndPreview();
                ((MainActivity) getActivity()).setActionState(MainActivity.PASSWORDSTATE);
                ((MainActivity)getActivity()).setCurrentFragment(MainActivity.PASSVERFRAG);
                ((MainActivity)getActivity()).replaceFragments(PasswordVerificationFragment.class);
            }
        });

        mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null){
                    Log.i(TAG, "Error creating media file.");
                    return;
                }
                if(Util.isExternalStorageWritable()) {
                    Log.i(TAG,"Device external storage is available for read and write.");
                    try {
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(data);
                        fos.close();

                        // rotate picture and save it back.
                        ((MainActivity)getActivity()).setFile(pictureFile);

                        File rotatedPic = rotatePicture();
                        ((MainActivity)getActivity()).setFile(rotatedPic);

                        // Only save taken face image.


                        ((MainActivity)getActivity()).setBinaryData(encodeImage(rotatedPic));

                        if( ((MainActivity)getActivity()).getActionState().equals(MainActivity.REGISTERSTATE) ||
                            ((MainActivity)getActivity()).getActionState().equals(MainActivity.UPDATESTATE)) {
                            // Switch to Register Fragment
                            releaseCameraAndPreview();
                            ((MainActivity) getActivity()).setCurrentFragment(MainActivity.REGISTERFRAG);
                            ((MainActivity) getActivity()).replaceFragments(RegisterFragment.class);
                        }
                        else if(((MainActivity)getActivity()).getActionState().equals(MainActivity.VERIFYSTATE)) {
                            verifyFace();
                        }
                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d(TAG, "Error accessing file: " + e.getMessage());
                    }
                }
            }
        };

        return view;
    }

    private File rotatePicture() {

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

//        create a file to write bitmap data
//        File f = new File(mediaStorageDir.getPath() + File.separator +
//                "IMG_"+ faceImgName + rotateImg + ".jpg");
        try {
            if(pictureFile.delete()) {
                Log.i(TAG, "delete landscape Img_FaceLock.jpg");
                }
            if(pictureFile.createNewFile()) {
                Log.i(TAG, "create portrait Img_FaceLock.jpg");
            }
//            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (finalBitmap != null) {
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        }
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(pictureFile);
//            fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pictureFile;
    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        if(!getActivity().isFinishing()) {
//            Log.i(TAG, "Activity is closing.");
//            if (getFragmentManager().findFragmentById(this.getId()) != null) {
//                Log.i(TAG, TAG + " destroy.");
//                getFragmentManager().beginTransaction().remove(this)
//                        .commit();
//            }
//        }
//    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private String encodeImage(File pictureFile) {


        Bitmap bm = BitmapFactory.decodeFile(pictureFile.toString());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }


//    private String toBinary(byte[] bytes)
//    {
//        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
//        for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
//            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
//        return sb.toString();
//    }

    private void verifyFace() {

        // Todo
        // request for verification
        // if true : return success, username, password, filepath
        // if false : return fail

        dialog = ProgressDialog.show(getActivity(),
                "Verifying", "It might take a few seconds...",true);
        dialog.setCancelable(false);
        dialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(dialog!=null) {
                    dialog.dismiss();
                }
            }
        }, 4000);

        verifySender sender = new verifySender();
        sender.execute(((MainActivity) getActivity()).createInfoJSON());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Create an instance of Camera
        if (CameraHardware.checkCameraHardware(getActivity())) {

            if (frontCameraId != -1) {
                mCamera = getCameraInstance(frontCameraId);
            }
        }
        // Create our Preview view and set it as the content of our activity.
        if (mCamera != null) {
            Log.i(TAG,"Add camera surface view to framelayout.");
            mPreview = new CameraPreview(getActivity(), mCamera);
        }
        else {
            Log.i(TAG, "Fail to add surfaceview to framelayout.");
        }
        camera_preview.addView(mPreview);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(dialog!=null){
            dialog.dismiss();
        }
        releaseCameraAndPreview();
    }

    public Camera getCameraInstance(int id) {
        Camera c = null;
        try {
            releaseCameraAndPreview();
            c = Camera.open(id); // attempt to get a Camera instance
        } catch (RuntimeException e) {
            Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
            c = Camera.open();
        }
        return c; // returns null if camera is unavailable
    }

    public void releaseCameraAndPreview() {
        camera_preview.removeAllViews();
        if (mCamera != null) {
            // mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /** Create a file Uri for saving an image or video*/
    private static Uri getOutputMediaFileUri(int type){
        Log.i(TAG,"URI = " + String.valueOf(Uri.fromFile(getOutputMediaFile(type))));
        return Uri.fromFile(getOutputMediaFile(type));
    }
    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ faceImgName + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ faceImgName + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private class verifySender extends AsyncTask<JSONObject, Void, String> {

        @Override
        protected String doInBackground(JSONObject... params) {
            String url = "http://163.28.17.73:8000/server/";
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
                        Log.i(TAG, tag + result.toString());
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
                String user = (String) returnInformation.get("username");
                String password = (String) returnInformation.get("passwd");
                String data = (String) returnInformation.get("face");

                if (!success) {
                    Toast.makeText(getActivity(), "Not yet register ?",Toast.LENGTH_SHORT).show();
                } else {
                    mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                            "IMG_"+ faceImgName + ".jpg");
                    if (mediaFile.delete()) {
                        try {
                            if(!mediaFile.createNewFile()) {
                                Log.i(TAG, "load fail.");
                            }
                            FileOutputStream fos = new FileOutputStream(mediaFile);
                            fos.write(Base64.decode(data, Base64.DEFAULT));
                            fos.close();
                            Log.i(TAG, "load image from server.");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    Toast.makeText(getActivity(),"Hello, "+user+"!!",Toast.LENGTH_SHORT).show();

                    if(dialog!=null) {dialog.dismiss();}
                    if(((MainActivity) getActivity()).getLaunchBy().equals(MainActivity.SCREEENON)) {
                        Log.i(TAG,tag+"Verification for screenOn success, closing App.");
                        ((MainActivity)getActivity()).unlockDevice();
                    }
                    ((MainActivity) getActivity()).setUser(user);
                    ((MainActivity) getActivity()).setPass(password);
                    ((MainActivity) getActivity()).setFile(mediaFile);

                    releaseCameraAndPreview();
                    ((MainActivity) getActivity()).setActionState(MainActivity.IDLESTATE);
                    ((MainActivity) getActivity()).setCurrentFragment(MainActivity.LOBBYFRAG);
                    ((MainActivity) getActivity()).replaceFragments(LobbyFragment.class);
                }

            } catch (JSONException e) {
                System.out.println("unable to catch response\n");
            }

        }
    }
}