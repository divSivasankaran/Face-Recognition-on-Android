package conex.facerecognition;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.util.Pair;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by div_1 on 27/9/2017.
 */

public class GlobalClass extends Application {
    private FaceRecognizer mFaceRecognizer = null;
    private String current_method = null;
    public FaceRecognizer getFaceRecognizer() {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String method = SP.getString("pref_frMethod", "facenet");
        if(current_method == null|| !current_method.equals(method))
        {
            this.mFaceRecognizer = null;
            current_method = method;
            Toast.makeText(getApplicationContext(), "Using " + current_method, Toast.LENGTH_SHORT).show();
        }
        if (this.mFaceRecognizer == null)
        {
            if(method.equals("opencv")) {
                this.mFaceRecognizer = new CVFaceRecognizer();
                String ModelFile = getFilesDir() + File.separator + getString(R.string.CVModelFileName);
                if(this.mFaceRecognizer.setModelFile(ModelFile)==false) {
                    try {
                        Mat barackimg = Utils.loadResource(getApplicationContext(), R.drawable.barack, Highgui.IMREAD_GRAYSCALE);
                        List<Pair<Mat, String>> mTrain = new ArrayList<>();
                        mTrain.add(new Pair<>(barackimg, "Barack"));
                        this.mFaceRecognizer.train(mTrain);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (method.equals("facenet"))
            {
                File model = new File(getString(R.string.facenetModel));
                if(model.exists())
                {
                    this.mFaceRecognizer = new Facenet(getApplicationContext());
                    //TODO: Load the db/trained model file with user data into modelFile
                }
            }
        }
        return this.mFaceRecognizer;
    }
}
