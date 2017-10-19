#include "FaceRecognizer_jni.h"
#include "opencv2/core/core.hpp"
#include "opencv2/contrib/contrib.hpp"
#include "opencv2/highgui/highgui.hpp"

#include <iostream>
#include <fstream>
#include <sstream>

#include <string>
#include <vector>

#include <android/log.h>

#define LOG_TAG "FaceRecognizer"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;


JNIEXPORT jlong JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeCreateObject
(JNIEnv * jenv, jclass)
{
    LOGD("Java_conex_facerecognition_CVFaceRecognizer_nativeCreateObject enter");
    jlong  result = 0;
    try
    {
        Ptr<FaceRecognizer> model = createLBPHFaceRecognizer();
        model.addref();
        result = (jlong)(model.obj);
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeCreateObject caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of CVFaceRecognizer.nativeCreateObject()");
    }

    LOGD("Java_conex_facerecognition_CVFaceRecognizer_nativeCreateObject exit");
    return result;
}

JNIEXPORT void JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeDestroyObject
(JNIEnv * jenv, jclass, jlong thiz)
{
    LOGD("Java_conex_facerecognition_CVFaceRecognizer_nativeDestroyObject enter");
    try
    {
        if(thiz != 0)
        {
            delete (FaceRecognizer*)thiz;
        }
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeestroyObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeDestroyObject caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of CVFaceRecognizer.nativeDestroyObject()");
    }
    LOGD("Java_conex_facerecognition_CVFaceRecognizer_nativeDestroyObject exit");
}

JNIEXPORT void JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeSave
(JNIEnv * jenv, jclass, jlong thiz, jstring filename)
{
    LOGD("Java_conex_facerecognition_CVFaceRecognizer_nativeSave enter");
    try
    {
        const char* jnamestr = jenv->GetStringUTFChars(filename, NULL);
        string c_filename(jnamestr);
        FaceRecognizer* model = ((FaceRecognizer*)(thiz));
        string name = model->info()->name();
        model->save(c_filename);
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeStart caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeSave caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of CVFaceRecognizer.nativeSave()");
    }
    LOGD("Java_conex_facerecognition_CVFaceRecognizer_nativeSave exit");
}

JNIEXPORT void JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeLoad
(JNIEnv * jenv, jclass, jlong thiz, jstring filename)
{
    LOGD("Java_conex_facerecognition_CVFaceRecognizer_nativeLoad enter");
    try
    {
        const char* jnamestr = jenv->GetStringUTFChars(filename, NULL);
        string c_filename(jnamestr);
        FaceRecognizer* model = ((FaceRecognizer*)(thiz));
        string name = model->info()->name();
        model->load(c_filename);
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeStop caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeStop caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of CVFaceRecognizer.nativeLoad()");
    }
    LOGD("Java_conex_facerecognition_CVFaceRecognizer_nativeLoad exit");
}


JNIEXPORT jboolean JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeTrain
(JNIEnv * jenv, jclass, jlong thiz, jlongArray images, jobjectArray labels, jstring filename)
{
    bool final = false;
    LOGD("Java_conex_facerecognition_CVFaceRecognizer__nativeTrain enter");
    try {
        //Train clears all the existing information anyway.
        // So we re-create the label info based on the current training set
        size_t objCount = (size_t) (jenv->GetArrayLength(images));
        vector<Mat> c_images;
        map<int,string> c_labels;
        map<string,int> r_labels;
        vector<int> c_ids;
        for (int i = 0; i < (int) objCount; i++)
        {
            jstring jlabel = (jstring)(jenv->GetObjectArrayElement(labels,i));
            const char* jnamestr = jenv->GetStringUTFChars(jlabel, NULL);
            string label(jnamestr);
            //assign an id to the label if it doesn't exist.
            int id = -1;
            if(r_labels.find(label)==r_labels.end())
            {
                id = r_labels.size();
                r_labels[label] = id;
                c_labels[id]= label;
            }
            else{
                id = r_labels[label];
            }

            c_ids.push_back(id);
            Mat jImage = *((Mat*) (jenv->GetLongArrayElements(images,false)[i]));
            c_images.push_back(jImage);
        }
        const char* jnamestr = jenv->GetStringUTFChars(filename, NULL);
        string c_filename(jnamestr);

        FaceRecognizer* model = ((FaceRecognizer*)(thiz));
        string name = model->info()->name();
        model->train(c_images,c_ids);
        model->setLabelsInfo(c_labels);
        model->save(c_filename);
        final = true;
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeTrain caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeTrain caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code CVFaceRecognizer.nativeTrain()");
    }
    LOGD("Java_conex_facerecognition_CVFaceRecognizer_nativeTrain exit");
    return (jboolean)final;
}

JNIEXPORT jstring JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativePredict
        (JNIEnv * jenv, jclass, jlong thiz, jlong image, jlong threshold)
{
    string result = "unknown";
    LOGD("Java_conex_facerecognition_CVFaceRecognizer__nativeTrain enter");
    try {
        Mat jImage = *((Mat*) (image));
        FaceRecognizer* model = ((FaceRecognizer*)(thiz));
        string name = model->info()->name();
        int id = -1;
        double confidence = 0.0;
        long c_threshold = (long) (threshold);
        model->predict(jImage, id, confidence);
        if(confidence < threshold)
        {
            result.clear();
            result = model->getLabelInfo(id);
        }

    }
    catch(cv::Exception& e)
    {
        LOGD("nativeTrain caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeTrain caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code CVFaceRecognizer.nativeTrain()");
    }
    LOGD("Java_conex_facerecognition_CVFaceRecognizer_nativeTrain exit");
    return jenv->NewStringUTF(result.c_str());
}

JNIEXPORT jboolean JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeUpdate
        (JNIEnv * jenv, jclass, jlong thiz, jlongArray images, jobjectArray labels, jstring filename)
{
    bool final = false;
    LOGD("Java_conex_facerecognition_CVFaceRecognizer__nativeUpdate enter");
    try {
        //Upate just adds to the  existing model.
        // So we have to maintain the label info from the original model
        map<int,string> c_labels;
        map<string,int> r_labels;
        FaceRecognizer* model = ((FaceRecognizer*)(thiz));
        vector<int> id_list = model->getLabelsByString("");
        for(int i=0; i<id_list.size(); i++)
        {
            string s = model->getLabelInfo(id_list[i]);
            r_labels[s] = id_list[i];
            c_labels[i] = s;
        }

        size_t objCount = (size_t) (jenv->GetArrayLength(images));
        vector<Mat> c_images;
        vector<int> c_ids;
        for (int i = 0; i < (int) objCount; i++)
        {
            jstring jlabel = (jstring)(jenv->GetObjectArrayElement(labels,i));
            const char* jnamestr = jenv->GetStringUTFChars(jlabel, NULL);
            string label(jnamestr);
            //assign an id to the label if it doesn't exist.
            int id = -1;
            if(r_labels.find(label)==r_labels.end())
            {
                id = r_labels.size();
                r_labels[label] = id;
                c_labels[id]= label;
            }
            else{
                id = r_labels[label];
            }
            c_ids.push_back(id);
            Mat jImage = *((Mat*) (jenv->GetLongArrayElements(images,false)[i]));
            c_images.push_back(jImage);
        }
        const char* jnamestr = jenv->GetStringUTFChars(filename, NULL);
        string c_filename(jnamestr);
        string name = model->info()->name();
        model->update(c_images,c_ids);
        model->setLabelsInfo(c_labels);
        model->save(c_filename);
        final = true;
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeTrain caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeUpdate caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code CVFaceRecognizer.nativeUpdate()");
    }
    LOGD("Java_conex_facerecognition_CVFaceRecognizer_nativeUpdate exit");
    return (jboolean)final;
}