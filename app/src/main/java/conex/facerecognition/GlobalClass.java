package conex.facerecognition;

import android.app.Application;

/**
 * Created by div_1 on 27/9/2017.
 */

public class GlobalClass extends Application {
    private CVFaceRecognizer mCVFaceRecognizer = null;

    public CVFaceRecognizer getCVFaceRecognizer() {
        if (this.mCVFaceRecognizer == null)
        {
            this.mCVFaceRecognizer = new CVFaceRecognizer();
        }
        return this.mCVFaceRecognizer;
    }
}
