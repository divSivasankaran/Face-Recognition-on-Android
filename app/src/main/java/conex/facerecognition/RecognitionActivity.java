package conex.facerecognition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.EditText;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.Point;

import conex.facerecognition.util.ImageUtil;

public class RecognitionActivity extends AppCompatActivity  implements CvCameraViewListener2{

    private CameraBridgeViewBase   mOpenCvCameraView;
    private int                    mCameraId           = 0;
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(190, 241, 243, 255);//new Scalar(190, 241, 243, 255);
    private static final Scalar    FACE_TEXT_COLOR     = new Scalar(190, 241, 243, 255);
    private static final Scalar    FACE_TEXT_BACKGROUND  = new Scalar(0, 0, 0, 255);
    public  static final int       JAVA_DETECTOR       = 0;
    public  static final int       NATIVE_DETECTOR     = 1;
    private static final double    FONT_SIZE           = 3;
    private static final int       THICKNESS           = 4;

    private Snackbar               mManyFacesErrorMsg;
    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;

    private FaceTracker            mNativeDetector;
    private int                    mDetectorType       = NATIVE_DETECTOR;
    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
    private int                    mCount              = 0;
    private String                 mUserLabel;
    private List<Pair<Mat,String>> mTrain              = new ArrayList<>();
    private List<Pair<Mat,String>> mTrainingSet        = new ArrayList<>();
    private boolean                mTrainingInProgress = false;
    private boolean                mEnrollUser         = false;
    private AlertDialog            mEnrollDialog       = null;

    // Used to load the 'native-lib' library on application startup.
    static {
        try{
            System.loadLibrary("native-lib");

        }catch (Exception e)
        {
            e.printStackTrace();
            Log.e("ERROR", "Loading native library fails" + e);
        }
    }

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton switch_cam = (FloatingActionButton) findViewById(R.id.button_switch_camera);
        switch_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RecognitionActivity.this.swapCamera();
            }
        });

        FloatingActionButton enroll = (FloatingActionButton) findViewById(R.id.button_enroll);
        enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecognitionActivity.this.getUserLabel();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        }

        Log.i("DEBUG", "on create STARTED successfully");
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_preview_opencv);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        GlobalClass global = (GlobalClass)getApplication();

        mManyFacesErrorMsg = Snackbar.make(findViewById(R.id.error_enroll_many_faces), getString(R.string.enroll_many_faces_error), Snackbar.LENGTH_LONG)
                .setAction("Ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Perform anything for the action selected
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.colorText));


        View snackbarView = mManyFacesErrorMsg.getView();
        snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        Log.i("DEBUG", "on create loaded successfully");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_overflowmenu));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_enrollUser) {
            getUserLabel();
        }
        else if(id == R.id.action_settings){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void getUserLabel()
    {
        if(mEnrollDialog==null){
            final EditText taskEditText = new EditText(this);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                    .setTitle("Who are we enrolling?")
                    .setView(taskEditText)
                    .setPositiveButton("Enroll", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RecognitionActivity.this.mUserLabel = String.valueOf(taskEditText.getText());
                            RecognitionActivity.this.mEnrollUser = true;
                            RecognitionActivity.this.mCount = 0;
                            RecognitionActivity.this.mTrainingSet.clear();

                        }
                    })
                    .setNegativeButton("Cancel",  new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            RecognitionActivity.this.mEnrollUser = false;
                            RecognitionActivity.this.mTrainingInProgress = false;
                            RecognitionActivity.this.mTrainingSet.clear();
                            RecognitionActivity.this.mTrain.clear();
                        }
                    });
            mEnrollDialog = alertDialogBuilder.create();
        }

        mEnrollDialog.show();
    }

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("DEBUG", "OpenCV loaded successfully");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e("DEBUG", "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i("DEBUG", "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        mNativeDetector = new FaceTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("DEBUG", "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();

                    } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {

            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {

            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
//        OpenCVLoader.initAsync( OpenCVLoader.OPENCV_VERSION_2_4_8,
//                this, mLoaderCallback );
    }
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {

    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) { mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        else if (mDetectorType == NATIVE_DETECTOR) {
            if (mNativeDetector != null)
                mNativeDetector.detect(mGray, faces);
        }
        else {
            Log.e("DEBUG", "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();
        double maxArea  = 0;
        int maxId = 0;
        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
            if(facesArray[i].area()>maxArea)
            {
                maxArea = facesArray[i].area();
                maxId = i;
            }
            if(!mTrainingInProgress && !mEnrollUser && (mEnrollDialog==null || !mEnrollDialog.isShowing()))
            {
                GlobalClass global = (GlobalClass) getApplication();
                String id = global.getFaceRecognizer().predict(ImageUtil.getCroppedFace(mRgba, facesArray[i]));
                Point loc = facesArray[i].tl().clone();
                loc.y = loc.y - 5;
                setLabel(mRgba, id, loc);
            }
        }

        if(facesArray.length!=0 && mEnrollUser && !mTrainingInProgress) {

            if (mManyFacesErrorMsg.isShown())
                mManyFacesErrorMsg.dismiss();
            //Adding the detected face to the training set
            mTrainingInProgress = true;
            for(mCount = 0; mCount<20; mCount++) {
                mTrain.add(new Pair(ImageUtil.getCroppedFace(mRgba, facesArray[maxId]), mUserLabel));
            }
            train();
        }
            return mRgba;
    }

    private void setLabel(Mat img, String label, Point pt )
    {
        Size text = Imgproc.getTextSize(label, Core.FONT_HERSHEY_PLAIN, FONT_SIZE, THICKNESS, null);
        Point dst =  new Point(0,0);
        dst.x = pt.x + text.width;
        dst.y = pt.y -  text.height;
        Imgproc.rectangle(img, pt, dst, FACE_TEXT_BACKGROUND,-1);
        Imgproc.putText(img, label, pt, Core.FONT_HERSHEY_PLAIN, FONT_SIZE, FACE_TEXT_COLOR, THICKNESS);
    }
    private void train()
    {
        mTrainingSet.addAll(mTrain);
        mTrainingInProgress = true;
        GlobalClass global = (GlobalClass)getApplication();
        global.getFaceRecognizer().update(mTrainingSet);
        mTrainingInProgress = false;
        mEnrollUser = false;
    }

    private void swapCamera(){
        mCameraId = mCameraId^1;
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(mCameraId);
        mOpenCvCameraView.enableView();
    }

}
