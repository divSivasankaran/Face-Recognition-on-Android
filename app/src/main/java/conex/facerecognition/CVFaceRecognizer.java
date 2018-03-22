package conex.facerecognition;

import android.support.v4.util.Pair;

import org.opencv.core.Mat;

import java.io.File;
import java.util.List;

/**
 * Created by div_1 on 26/9/2017.
 */

public class CVFaceRecognizer extends FaceRecognizer {
    public CVFaceRecognizer(){
        mNativeObj = nativeCreateObject();
        mModelFile = null;
    }

    public void load()
    {
        nativeLoad(mNativeObj,mModelFile);
    }

    public void save() {
        nativeSave(mNativeObj, mModelFile);
    }

    public boolean train(List<Pair<Mat,String>> trainingSet) {
        long[] images = new long[trainingSet.size()];
        String[] labels = new String [trainingSet.size()];
        for(int i = 0; i < trainingSet.size(); i++)
        {
            Pair<Mat,String> training_point = trainingSet.get(i);
            images[i] = training_point.first.getNativeObjAddr();
            labels[i] = training_point.second;
        }
        nativeTrain(mNativeObj,images,labels,mModelFile);
        return true;
    }

    public String predict(Mat img){
        String result = "";
        try {
            result = nativePredict(mNativeObj, img.getNativeObjAddr(), mThreshold);
        } catch(Exception e){
        }

        return result;
    }

    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    public boolean update(List<Pair<Mat,String>> trainingSet) {
        long[] images = new long[trainingSet.size()];
        String[] labels = new String [trainingSet.size()];
        for(int i = 0; i < trainingSet.size(); i++)
        {
            Pair<Mat,String> training_point = trainingSet.get(i);
            images[i] = training_point.first.getNativeObjAddr();
            labels[i] = training_point.second;
        }
        nativeUpdate(mNativeObj,images,labels, mModelFile);
        return true;
    }


    private long mNativeObj = 0;
    private long mThreshold = 70;
    private static native long nativeCreateObject();
    private static native void nativeDestroyObject(long thiz);
    private static native void nativeLoad(long thiz, String filename);
    private static native void nativeSave(long thiz, String filename);
    private static native boolean nativeTrain(long thiz, long[] images, String[] labels, String filename);
    private static native String nativePredict(long thiz, long image, long threshold);
    private static native boolean nativeUpdate(long thiz, long[] images, String[] labels, String filename);
}
