package conex.facerecognition;

/**
 * Created by div_1 on 14/3/2018.
 */

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.support.v4.util.Pair;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;


public class Facenet extends FaceRecognizer {
    private Interpreter tflite;

    private int batchSize = 1;
    private int inputSize = 160;
    private int channels  = 3;
    private int outputSize = 128;

    private int[] intValues;
    private ByteBuffer imgData = null;
    private HashMap<String,Mat> mUserData = new HashMap<>();
    private HashMap<String,Integer> mMapCounter = new HashMap<>();
    private double mThreshold = 0.0;
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
            AssetManager assetManager = context.getAssets();
            long startTime = SystemClock.uptimeMillis();
            tflite = new Interpreter(loadModelFile(assetManager, modelFilename));
            imgData =
                    ByteBuffer.allocateDirect(4*
                            batchSize * inputSize * inputSize * channels);
            imgData.order(ByteOrder.nativeOrder());
            intValues = new int[inputSize * inputSize];
//            clear();
            load();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
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
        convertToByteBuffer(img);
        float[][] outputs_ = new float[1][outputSize];
        tflite.run(imgData,outputs_);
        return outputs_[0];
    }

    private void convertToByteBuffer(Mat img)
    {
        long startTime = SystemClock.uptimeMillis();

        if (imgData == null) {
            return;
        }
        imgData.rewind();
        Bitmap bitmap = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bitmap);
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        int pixel = 0;

        //Pre-whiten Image
        MatOfDouble meanMat = new MatOfDouble();
        MatOfDouble stdMat = new MatOfDouble();
        Core.meanStdDev(img, meanMat, stdMat);
        double[] means = meanMat.get(0, 0);
        double[] stds = stdMat.get(0, 0);
        float mean = (float)means[0];
        float std = (float)stds[0];

        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                final int val = intValues[pixel++];
                imgData.putFloat((byte)  (((float)((val >> 16) & 0xFF)) - mean) / std);
                imgData.putFloat((byte) (((float)((val >> 8) & 0xFF)) - mean) / std);
                imgData.putFloat((byte) (((float)(val & 0xFF)) - mean) / std);
            }
        }

        long endTime = SystemClock.uptimeMillis();
        Log.d("DEBUG", "Convert to byte buffer: " + Long.toString(endTime - startTime));
    }


    private float[][][][] convertToFloat4D(Mat img){

        long startTime = SystemClock.uptimeMillis();
        Bitmap bmp = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bmp);
        bmp.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize);

        //Pre-whiten Image
        MatOfDouble meanMat = new MatOfDouble();
        MatOfDouble stdMat = new MatOfDouble();
        Core.meanStdDev(img, meanMat, stdMat);
        double[] means = meanMat.get(0, 0);
        double[] stds = stdMat.get(0, 0);
        float mean = (float)means[0];
        float std = (float)stds[0];

        float[][][][] floatValues = new float[1][inputSize][inputSize][channels];
        int pixel = 0;
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                final int val = intValues[pixel++];
                floatValues[0][i][j][0] = (((float) ((val >> 16) & 0xFF)) - mean) / std;
                floatValues[0][i][j][1] = (((float) ((val >> 8) & 0xFF)) - mean) / std;
                floatValues[0][i][j][2] = (((float) (val & 0xFF)) - mean) / std;
            }
        }
        long endTime = SystemClock.uptimeMillis();
        Log.d("DEBUG", "Convert to float 4D array: " + Long.toString(endTime - startTime));
        return floatValues;
    }

    private float[] convertToFloat(Mat img){

        long startTime = SystemClock.uptimeMillis();
        Bitmap bmp = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bmp);
        bmp.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize);

        //Pre-whiten Image
        MatOfDouble meanMat = new MatOfDouble();
        MatOfDouble stdMat = new MatOfDouble();
        Core.meanStdDev(img, meanMat, stdMat);
        double[] means = meanMat.get(0, 0);
        double[] stds = stdMat.get(0, 0);
        float mean = (float)means[0];
        float std = (float)stds[0];

        float[] floatValues = new float[inputSize * inputSize * channels];
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3 + 0] = (((float)((val >> 16) & 0xFF)) - mean) / std;
            floatValues[i * 3 + 1] = (((float)((val >> 8) & 0xFF)) - mean) / std;
            floatValues[i * 3 + 2] = (((float)(val & 0xFF)) - mean) / std;
        }
        long endTime = SystemClock.uptimeMillis();
        Log.d("DEBUG", "Convert to float array: " + Long.toString(endTime - startTime));
        return floatValues;
    }

    public void load(){
        try {
            File yourFile = new File(currentContext.getFilesDir().getPath().toString() + "/user.feat");
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

            yourFile = new File(currentContext.getFilesDir().getPath().toString() + "user.count");
            fileInputStream = new FileInputStream(yourFile);
            objectInputStream = new ObjectInputStream(fileInputStream);

            HashMap<String,Integer> s = (HashMap<String,Integer>) objectInputStream.readObject();
            for(String key:s.keySet())
            {
                mMapCounter.put(key,s.get(key));
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
            File yourFile = new File(currentContext.getFilesDir().getPath().toString() + "/user.feat");
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

            yourFile = new File(currentContext.getFilesDir().getPath().toString() + "user.count");
            fileOutputStream = new FileOutputStream(yourFile);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);

            HashMap<String,Integer> convertedUserMap = new HashMap<>();
            for(String key:mMapCounter.keySet())
            {
                convertedUserMap.put(key,mMapCounter.get(key));
            }
            objectOutputStream.writeObject(convertedUserMap);
            objectOutputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void clear(){
        try {
            File yourFile = new File(currentContext.getFilesDir().getPath().toString() + "/user.feat");
            yourFile.createNewFile(); // if file already exists will do nothing
            yourFile.delete();
            yourFile = new File(currentContext.getFilesDir().getPath().toString() + "/user.count");
            yourFile.createNewFile(); // if file already exists will do nothing
            yourFile.delete();
            yourFile = new File(currentContext.getFilesDir().getPath().toString() + "/user.db");
            yourFile.createNewFile(); // if file already exists will do nothing
            yourFile.delete();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean train(List<Pair<Mat,String>> trainingSet) {
        for (int i=0; i< trainingSet.size(); i++)
        {
            Mat face = trainingSet.get(i).first;
            String name = trainingSet.get(i).second;
            MatOfFloat res = new MatOfFloat();
            res.fromArray(getFeatureVector(face));
            Mat resMat = new Mat();
            res.copyTo(resMat);
            Core.normalize(resMat,resMat);
            if(!mUserData.containsKey(name))
            {
                mUserData.put(name,resMat);
                mMapCounter.put(name,1);
            }
            else {
                Mat temp = mUserData.get(name);
                mMapCounter.put(name,mMapCounter.get(name)+1);
                Core.add(temp, resMat, temp);
            }
        }

        save();
        return true;
    }

    public double cosine_distance(Mat feature_a, Mat feature_b)
    {
        double ab = feature_a.dot(feature_b);
        double aa = feature_a.dot(feature_a);
        double bb = feature_b.dot(feature_b);
        return ab / Math.sqrt(aa*bb);
    }

    public String predict(Mat img){

        String result = "Don't know yo!";

        if(mUserData.size()>0) {
            MatOfFloat res = new MatOfFloat();
            res.fromArray(getFeatureVector(img));
            Mat resMat = new Mat();
            res.copyTo(resMat);
            Core.normalize(resMat,resMat);

            double max = 0;

            for (String key : mUserData.keySet()) {
                Mat userFeat = mUserData.get(key);
                Core.divide(userFeat, new Scalar(mMapCounter.get(key)),userFeat);

                double dist = cosine_distance(res, userFeat);
                if (dist > mThreshold && dist > max) {
                    max = dist;
                    result= key;
                }
            }
        }

        return result;
    }

    public boolean update(List<Pair<Mat,String>> trainingSet){
        return train(trainingSet);
    }
}


