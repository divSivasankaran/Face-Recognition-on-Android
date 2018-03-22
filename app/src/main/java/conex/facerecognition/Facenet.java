package conex.facerecognition;

/**
 * Created by div_1 on 14/3/2018.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import conex.facerecognition.util.MatofFloatConverter;
import conex.facerecognition.util.User;
import conex.facerecognition.util.UserDatabase;

public class Facenet extends FaceRecognizer {
    private TensorFlowInferenceInterface inferenceInterface;
    // Config values.
    private String inputLayer = "input";
    private String outputLayer = "embeddings";

    private int inputSize = 160;
    private int channels  = 3;
    private int imageMean = 128;
    private int imageStd  = 128;
    private int outputSize = 128;

    private boolean logStats = true;
    private HashMap<String,Mat> mUserData = new HashMap<>();
    private double mThreshold = 1.1;
    public transient Context currentContext = null;
    /**
     * Initializes a native TensorFlow session for extracting Facenet features
     *
     * @param context The application context.
     * @param modelFilename The filepath of the model GraphDef protocol buffer.
     * @throws IOException
     */
    public Facenet(Context context, String modelFilename)
    {
        try{
            currentContext = context;
            inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), modelFilename);
            load();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Initializes a native TensorFlow session for extracting Facenet features
     * Uses default facenet_inception.pb that is loaded along with application assets
     * @param context The application context.
     * @throws IOException
     */
    public Facenet(Context context)
    {
        this(context,context.getString(R.string.facenetModel));
    }



    public float[] getFeatureVector(Mat img){

        Imgproc.resize(img, img, new Size(inputSize, inputSize));

        inferenceInterface.feed(inputLayer, getPixels(img), 1, inputSize, inputSize, channels);
        String phase_train = "phase_train";
        inferenceInterface.feed(phase_train,new boolean[]{false});
        inferenceInterface.run(new String[]{outputLayer}, logStats);
        float[] outputs = new float[outputSize];
        inferenceInterface.fetch(outputLayer, outputs);

        return outputs;
    }

    public double getSimilarity(List<Float> a,List<Float> b ){

        double dist = 0.0;

        if(a!=null && b!=null && a.size()==b.size()){
            for(int i = 0; i< a.size(); i++)
            {
                dist += Math.pow(a.get(i)-b.get(i),2);
            }
           return 1 - Math.sqrt(dist);
        }
        return -1.0;
    }

    private float[] getPixels(Mat img){
        Bitmap bmp = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bmp);
        int[] intValues = new int[inputSize * inputSize];
        bmp.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize);

        float[] floatValues = new float[inputSize * inputSize * channels];
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3 + 0] = (((float)((val >> 16) & 0xFF)) - imageMean) / imageStd;
            floatValues[i * 3 + 1] = (((float)((val >> 8) & 0xFF)) - imageMean) / imageStd;
            floatValues[i * 3 + 2] = (((float)(val & 0xFF)) - imageMean) / imageStd;
        }
        return floatValues;
    }
    public void load(){
        try {
            File yourFile = new File(currentContext.getFilesDir().getPath().toString() + "/user.db");
            FileInputStream fileInputStream = new FileInputStream(yourFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            HashMap<String,List<Float>> t = (HashMap<String,List<Float>>) objectInputStream.readObject();
            for(String key:t.keySet())
            {
                MatOfFloat mat = new MatOfFloat();
                mat.fromList(t.get(key));
                Mat m = new Mat();
                mat.copyTo(m);
                mUserData.put(key,m);
            }
            objectInputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public void save(){
        try {
            File yourFile = new File(currentContext.getFilesDir().getPath().toString() + "/user.db");
            yourFile.createNewFile(); // if file already exists will do nothing
            FileOutputStream fileOutputStream = new FileOutputStream(yourFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            HashMap<String,List<Float>> convertedUserData = new HashMap<>();
            for(String key:mUserData.keySet())
            {
                MatOfFloat t = new MatOfFloat(mUserData.get(key));
                convertedUserData.put(key,t.toList());
            }

            objectOutputStream.writeObject(convertedUserData);
            objectOutputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean train(List<Pair<Mat,String>> trainingSet) {
        for (Pair<Mat, String> element : trainingSet) {
            MatOfFloat res = new MatOfFloat();
            res.fromArray(getFeatureVector(element.first));
            Mat resMat = new Mat();
            res.copyTo(resMat);
            Core.normalize(resMat,resMat);
            if(!mUserData.containsKey(element.second))
            {
                mUserData.put(element.second,resMat);
            }
//            else {
//                Mat temp = mUserMap.get(element.second);
//                mMapCounter.put(element.second,mMapCounter.get(element.second)+1);
//                Core.add(temp, resMat, temp);
//            }
        }
//        for(String key:mUserMap.keySet())
//        {
//            Mat temp = mUserMap.get(key);
//            Scalar val = new Scalar(1/mMapCounter.get(key).doubleValue(),1,1,1);
//            Core.multiply(temp,val,temp);
//        }
        save();
        return true;
    }

    public String predict(Mat img){

        String result = "Don't know yo!";

        if(mUserData.size()>0) {
            MatOfFloat res = new MatOfFloat();
            res.fromArray(getFeatureVector(img));
            Mat resMat = new Mat();
            res.copyTo(resMat);
            double min = Math.sqrt(128);
            for (String key : mUserData.keySet()) {
                double dist = Core.norm(res, mUserData.get(key), Core.NORM_L2);
                if ( dist < min) {
                    min = dist;
                    result = key;
                }
            }
        }

        return result;
    }

    public boolean update(List<Pair<Mat,String>> trainingSet){
        return train(trainingSet);
    }
}


