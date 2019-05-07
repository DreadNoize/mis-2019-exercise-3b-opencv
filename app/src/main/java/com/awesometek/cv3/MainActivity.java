package com.awesometek.cv3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.Rect;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.osgi.OpenCVNativeLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    static{
        System.loadLibrary("opencv_java4");
    }

    private static final String TAG = "OCVSample::Activity";

    private static int PERMISSION_CAMERA = 0;

    private CameraBridgeViewBase cameraView;
    private Boolean cameraPermissionGranted = false;
    private Mat rgbaOutput = new Mat(0, 0, CvType.CV_8UC4);
    private CascadeClassifier faceClassifier;

    // currently not in use - left for showcasing purposes
    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            if(status == LoaderCallbackInterface.SUCCESS) {
                createCameraView();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Could not init OpenCV at all", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String cascadeFile = initAssetFile("haarcascade_frontalface_default.xml");
        faceClassifier = new CascadeClassifier(cascadeFile);
        if (faceClassifier.empty()) {
            Toast.makeText(this, "couldnt load cascade", Toast.LENGTH_LONG).show();
            faceClassifier = null;
        } else {
            Toast.makeText(this, "cascade successfully loaded", Toast.LENGTH_LONG).show();
        }


        //// this would load the library using the manager module
        // OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, loaderCallback);
        //// unfortunately, ,the jni libraries can only be initialized by using initDebug() directly
        //// we are aware that this happens on the main thread and can cause trouble
        //// in some rare situations
        if(!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(),
                    "Could not init OpenCV library", Toast.LENGTH_LONG).show();
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, loaderCallback);
        }
        createCameraView();
    }

    private void createCameraView() {
        if(!cameraPermissionGranted) {
            requestCameraPermission();
            return;
        }
        cameraView = findViewById(R.id.cameraView);
        cameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
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
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
    /*    Mat col  = inputFrame.rgba();
        Rect foo = new Rect(new Point(100,100), new Point(200,200));
        Imgproc.rectangle(col, foo.tl(), foo.br(), new Scalar(0, 0, 255), 3);
        return col;
    */
        Mat gray = inputFrame.gray();
        Mat col  = inputFrame.rgba();

        Mat tmp = gray.clone();
        Imgproc.Canny(gray, tmp, 80, 100);
        // Imgproc.cvtColor(tmp, col, Imgproc.COLOR_GRAY2RGBA, 4);

        MatOfRect faces = new MatOfRect();
        faceClassifier.detectMultiScale(gray, faces, 1.3);
        for(Rect face : faces.toArray()) {
            if(face.width < 20) {
                continue;
            }
            /*Imgproc.rectangle(col, new Point(face.x, face.y),
                    new Point(face.x + face.width, face.y + face.height),
                    new Scalar(255, 0, 0), 5);*/
            Point nosePosition = new Point(face.x + face.width * 0.5, face.y + face.height * 0.5);
            int noseSize = (int) (face.width * 0.1);
            Imgproc.circle(col, nosePosition, noseSize, new Scalar(255, 0, 0), Imgproc.FILLED);
        }
        return col;
    }

    public String initAssetFile(String filename)  {
        File file = new File(getFilesDir(), filename);
        if (!file.exists()) try {
            InputStream is = getAssets().open(filename);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data); os.write(data); is.close(); os.close();
        } catch (IOException e) { e.printStackTrace(); }
        Log.d(TAG,"prepared local file: "+filename);
        return file.getAbsolutePath();
    }
}
