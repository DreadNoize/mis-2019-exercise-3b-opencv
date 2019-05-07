package com.awesometek.cv3;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.osgi.OpenCVNativeLoader;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    static{
        System.loadLibrary("opencv_java4");
    }

    private static int PERMISSION_CAMERA = 0;

    private CameraBridgeViewBase cameraView;
    private Boolean cameraPermissionGranted = false;

    // currently not in use - left for showcasing purposes
    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            if(status == LoaderCallbackInterface.SUCCESS) {
                createCameraView();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Could not init OpenCV", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //// this would load the library using the manager module
        // OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, loaderCallback);
        //// unfortunately, ,the jni libraries can only be initialized by using initDebug() directly
        //// we are aware that this happens on the main thread and can cause trouble
        //// in some rare situations
        if(!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(),
                    "Could not init OpenCV", Toast.LENGTH_LONG).show();
        }
        // createCameraView();
    }

    private void createCameraView() {
        if(!cameraPermissionGranted) {
            requestCameraPermission();
            return;
        }
        cameraView = findViewById(R.id.cameraView);
        cameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        cameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        cameraView.setCvCameraViewListener(this);
        cameraView.enableView();
    }

    // location permission adapted from
    // https://github.com/googlemaps/android-samples/blob/master/tutorials/
    // CurrentPlaceDetailsOnMap/app/src/main/java/com/example/
    // currentplacedetailsonmap/MapsActivityCurrentPlace.java
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            cameraPermissionGranted = true;
            createCameraView();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    PERMISSION_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        cameraPermissionGranted = false;
        if(requestCode == PERMISSION_CAMERA && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraPermissionGranted = true;
            createCameraView();
        } else {
            requestCameraPermission();
        }
    }

    @Override
    public void onCameraViewStarted(int w, int h) {
        Log.w("MISDEBUG", "onCameraViewStarted got called. w = " + w + " and h = " + h);
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
    }
}
