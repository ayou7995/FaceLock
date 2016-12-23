package com.example.ayou7995.facelock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.AutoFocusCallback{

    private final static String Tag = "Jonathan";

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Button mTaskPicture;
    private Camera camera;

    private BootBroadcastReceiver bootBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);

        bootBroadcastReceiver = new BootBroadcastReceiver();
        IntentFilter bootFilter = new IntentFilter();
        bootFilter.addAction(Intent.ACTION_SCREEN_ON);
        bootFilter.addAction(Intent.ACTION_SCREEN_OFF);
        bootFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(bootBroadcastReceiver, bootFilter);

        initViews();
    }

    private void initViews() {
        mSurfaceView = (SurfaceView) this.findViewById(R.id.svPreview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mTaskPicture = (Button) this.findViewById(R.id.taskPicture);
        mTaskPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    camera.autoFocus(MainActivity.this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(bootBroadcastReceiver);
    }

    /** A safe way to get an instance of the Camera object. */
    /*public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }*/

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (success) {
            camera.takePicture(null, null, jpeg);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();

        if (Build.VERSION.SDK_INT >= 8)
            camera.setDisplayOrientation(90);

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size=getBestSize(parameters.getSupportedPreviewSizes());
        int w=size.width;
        int h=size.height;
        parameters.setPreviewSize(w, h);
        parameters.setPictureSize(w, h);
        parameters.set("orientation", "portrait");
        camera.setDisplayOrientation(90);
        camera.setParameters(parameters);
        try {
            //设置显示
            camera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            camera.release();
            camera = null;
        }
        //开始预览
        assert camera != null;
        camera.startPreview();
        //设置自动对焦
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    // success为true表示对焦成功，改变对焦状态图像
                }
            }
        });

        /*// 取得相機參數
        Camera.Parameters parameters = camera.getParameters();
        // 取得照片尺寸
        List supportedPictureSizes = parameters.getSupportedPictureSizes();
        int sptw = supportedPictureSizes.get(supportedPictureSizes.size() - 1).width;
        int spth = supportedPictureSizes.get(supportedPictureSizes.size() - 1).height;

        // 取得預覽尺寸
        List supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        int prvw = supportedPreviewSizes.get(0).width;
        int prvh = supportedPreviewSizes.get(0).height;

        parameters.setPictureFormat(PixelFormat.JPEG);
        parameters.setPreviewSize(640, 480);

        camera.setParameters(parameters);
        camera.startPreview();*/
    }

    private Camera.Size getBestSize(List<Camera.Size> supportedPreviewSizes) {
        Camera.Size largestSize=supportedPreviewSizes.get(0);
        int largestArea= supportedPreviewSizes.get(0).height*supportedPreviewSizes.get(0).width;
        for (Camera.Size s:supportedPreviewSizes){
            int area=s.width*s.height;
            if(area>largestArea){
                largestArea=area;
                largestSize=s;
            }
        }
        return largestSize;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    Camera.PictureCallback jpeg = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] imgData, Camera camera) {
            if (imgData != null) {
                Bitmap picture = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
                picture = rotationBitmap(picture);
                saveBitmap(picture);
            }

            camera.startPreview();
        }
    };

    public Bitmap rotationBitmap(Bitmap picture) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(picture,picture.getWidth(),picture.getHeight(),true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
        return rotatedBitmap;
    }

    public void saveBitmap(Bitmap bitmap) {

        FileOutputStream fOut;
        try {
            File dir = new File("/sdcard/demo/");
            if (!dir.exists()) {
                dir.mkdir();
            }

            String tmp = "/sdcard/demo/takepicture.jpg";
            fOut = new FileOutputStream(tmp);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

            try {
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            sendToGallery(this, tmp);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendToGallery(Context ctx, String path) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(path));
        intent.setData(contentUri);
        ctx.sendBroadcast(intent);
    }
}
