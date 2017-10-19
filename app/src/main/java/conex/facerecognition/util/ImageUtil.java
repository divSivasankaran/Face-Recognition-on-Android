package conex.facerecognition.util;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by div_1 on 29/9/2017.
 */

public class ImageUtil {
    private static Size mFaceSize  = new Size(100,100);
    public static Mat getCroppedFace(Mat img, Rect rect)
    {
        Mat croppedimage = img.submat(rect);
        Mat resizedimage = new Mat();
        Imgproc.resize(croppedimage, resizedimage, mFaceSize);
        Mat mGray = getGrayScale(resizedimage);
        return mGray;
    }
    public static Mat getGrayScale(Mat img)
    {
        Mat mGray = new Mat();
        Imgproc.cvtColor(img,mGray,Imgproc.COLOR_RGB2GRAY);
        return mGray;
    }
}
