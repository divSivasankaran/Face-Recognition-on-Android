package conex.facerecognition;
import android.support.v4.util.Pair;
import org.opencv.core.Mat;

import java.io.File;
import java.util.List;

/**
 * Created by div_1 on 15/3/2018.
 */

public abstract class FaceRecognizer {

    public boolean setModelFile(String filename)
    {
        mModelFile = filename;
        File f = new File(mModelFile);
        if(f.exists())
        {
            load();
            return true;
        }
        return false;
    }
    public void clearModelfile(String filename)
    {
        File f = new File(mModelFile);
        if(f.exists())
        {
            f.delete();
        }
    }

    public void load(String modelFile)
    {
        setModelFile(modelFile);
        load();
    }

    public void save(String filename){
        setModelFile(filename);
        save();
    }

    abstract public void load();
    abstract public void save();

    abstract public boolean train(List<Pair<Mat,String>> trainingSet);
    abstract public String predict(Mat imgg);
    abstract public  boolean update(List<Pair<Mat,String>> trainingSet);

    protected String mModelFile;
}
