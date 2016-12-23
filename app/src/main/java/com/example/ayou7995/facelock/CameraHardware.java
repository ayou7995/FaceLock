package com.example.ayou7995.facelock;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;

public class CameraHardware {
    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
}
