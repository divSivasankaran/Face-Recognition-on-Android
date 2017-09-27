package conex.facerecognition;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by div_1 on 25/9/2017.
 */

public class EnrollActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private CameraBridgeViewBase mOpenCvCameraView;
    private int                  mCameraId           = 0;
    private static final Scalar  FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    private static final double  FONT_SIZE           = 3;
    private static final int     THICKNESS           = 3;
    public  static final int     JAVA_DETECTOR       = 0;
    public  static final int     NATIVE_DETECTOR     = 1;

    private Mat                  mRgba;
    private Mat                  mGray;
    private File                 mCascadeFile;
    private CascadeClassifier    mJavaDetector;

    private Snackbar             mManyFacesErrorMsg;
    private FaceTracker          mNativeDetector;
    private int                  mDetectorType       = JAVA_DETECTOR;
    private float                mRelativeFaceSize   = 0.2f;
    private int                  mAbsoluteFaceSize   = 0;
    private Size                 mFaceSize           = new Size(100,100);
    private int                  mCount              = 0;
    private String               mUserLabel;
    private Map<String,Integer>  mUsers              = new HashMap<String, Integer>();;
    private Map<Integer,String>  mReverseUsers       = new HashMap<Integer,String>();
    private List<Pair<Mat,Integer>> mTrain           = new ArrayList<>();
    private List<Pair<Mat,Integer>> mTrainingSet     = new ArrayList<>();
    private boolean              mTrainingInProgress = false;
    private boolean              mTrainingCompleted  = false;
    private boolean              mNameEntered        = false;
    private int                  mUserCount          = 0;
    private AlertDialog          mEnrollDialog;
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageButton fab = (ImageButton) findViewById(R.id.button_enroll_switch_camera);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapCamera();
            }
        });

        getUserLabel();

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_preview_opencv);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mManyFacesErrorMsg = Snackbar.make(findViewById(R.id.error_enroll_many_faces), getString(R.string.enroll_many_faces_error), Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Perform anything for the action selected
                            }
                        })
                        .setActionTextColor(getResources().getColor(R.color.colorPrimary));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void getUserLabel()
    {
        final EditText taskEditText = new EditText(this);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Who are we enrolling?")
                .setView(taskEditText)
                .setPositiveButton("Enroll", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EnrollActivity.this.mUserLabel = String.valueOf(taskEditText.getText());
                        EnrollActivity.this.mNameEntered = true;
                    }
                })
                .setNegativeButton("Cancel",  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        mEnrollDialog = alertDialogBuilder.create();
        mEnrollDialog.show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_enrollUser) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                        mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");
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
        OpenCVLoader.initAsync( OpenCVLoader.OPENCV_VERSION_2_4_8,
                this, mLoaderCallback );
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

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) { mRgba = inputFrame.rgba();
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
        for (int i = 0; i < facesArray.length; i++)
        {
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
            //Start predicting
            if(mTrainingCompleted)
            {
                GlobalClass global = (GlobalClass)getApplication();
                int num = global.getCVFaceRecognizer().predict(getCroppedFace(mRgba,facesArray[i]));
                String id = String.valueOf(-1);
                if (mReverseUsers.get(num)!=null)
                    id = mReverseUsers.get(num);
                Core.putText(mRgba, id, facesArray[i].tl(), Core.FONT_HERSHEY_PLAIN, FONT_SIZE, FACE_RECT_COLOR, THICKNESS);
            }
        }

        if(!mTrainingCompleted && !mTrainingInProgress && mNameEntered) {
            if (facesArray.length > 1) {
                mManyFacesErrorMsg.show();
                //Restarting enrollment process
                mTrain.clear();
                mCount = 0;
            } else if (facesArray.length == 1) {
                if (mManyFacesErrorMsg.isShown())
                    mManyFacesErrorMsg.dismiss();
                //Adding the detected face to the training set

                mCount++;
                if (mCount % 10 == 0) {
                    if(mUsers.get(mUserLabel) == null)
                    {
                        mUsers.put(mUserLabel,mUserCount);
                        mReverseUsers.put(mUserCount++,mUserLabel);
                    }
                    mTrain.add(new Pair(getCroppedFace(mRgba, facesArray[0]), mUsers.get(mUserLabel)));
                }
                Core.putText(mRgba, String.valueOf(mCount / 10), facesArray[0].tl(), Core.FONT_HERSHEY_PLAIN, FONT_SIZE, FACE_RECT_COLOR, THICKNESS);
                if (mCount == 100) {
                    train();
                }
            }
        }

        return mRgba;
    }

    private Mat getCroppedFace(Mat img,Rect rect)
    {
        Mat croppedimage = img.submat(rect);
        Mat resizedimage = new Mat();
        Imgproc.resize(croppedimage, resizedimage, mFaceSize);
        Mat mGray = new Mat();
        Imgproc.cvtColor(resizedimage,mGray,Imgproc.COLOR_RGB2GRAY);
        return mGray;
    }

    private void train()
    {
        mTrainingSet.addAll(mTrain);
        if(mUserCount < 2)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getUserLabel();
                }
            });
            mNameEntered = false;
            mCount = 0;
            return;
        }
        else{
            mTrainingInProgress = true;
            GlobalClass global = (GlobalClass)getApplication();
            global.getCVFaceRecognizer().train(mTrainingSet);
            mTrainingInProgress = false;
            mTrainingCompleted = true;
        }
    }

    private void swapCamera(){
        mCameraId = mCameraId^1;
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(mCameraId);
        mOpenCvCameraView.enableView();
    }
}
