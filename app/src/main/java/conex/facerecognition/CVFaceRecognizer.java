package conex.facerecognition;

import android.support.v4.util.Pair;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

import java.util.List;

/**
 * Created by div_1 on 26/9/2017.
 */

public class CVFaceRecognizer {
    public CVFaceRecognizer(){
        mNativeObj = nativeCreateObject();
    }
    public void start() {
        nativeStart(mNativeObj);
    }

    public void stop() {
        nativeStop(mNativeObj);
    }

    public boolean train(List<Pair<Mat,Integer>> trainingSet) {
        long[] images = new long[trainingSet.size()];
        int[] labels = new int [trainingSet.size()];
        for(int i = 0; i < trainingSet.size(); i++)
        {
            Pair<Mat,Integer> training_point = trainingSet.get(i);
            images[i] = training_point.first.getNativeObjAddr();
            labels[i] = training_point.second.intValue();
        }
        nativeTrain(mNativeObj,images,labels);
        return true;
    }

    public int predict(Mat img) {

        int result = -1;
        try {

            result = nativePredict(mNativeObj, img.getNativeObjAddr());
        } catch(Exception e){
        }

        return result;
    }

    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    private long mNativeObj = 0;
    private static native long nativeCreateObject();
    private static native void nativeDestroyObject(long thiz);
    private static native void nativeStart(long thiz);
    private static native void nativeStop(long thiz);
    private static native boolean nativeTrain(long thiz, long[] images, int[] labels);
    private static native int nativePredict(long thiz, long image);
}
