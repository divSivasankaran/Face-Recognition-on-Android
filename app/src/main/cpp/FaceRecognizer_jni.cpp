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

JNIEXPORT void JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeStart
(JNIEnv * jenv, jclass, jlong thiz)
{
    LOGD("Java_conex_facerecognition_CVFaceRecognizer_nativeStart enter");
    try
    {
        //((DetectionBasedTracker*)thiz)->run();
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
        LOGD("nativeStart caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of CVFaceRecognizer.nativeStart()");
    }
    LOGD("Java_conex_facerecognition_CVFaceRecognizer_nativeStart exit");
}

JNIEXPORT void JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeStop
(JNIEnv * jenv, jclass, jlong thiz)
{
    LOGD("Java_conex_facerecognition_CVFaceRecognizer_nativeStop enter");
    try
    {
        //((DetectionBasedTracker*)thiz)->stop();
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
        jenv->ThrowNew(je, "Unknown exception in JNI code of CVFaceRecognizer.nativeStop()");
    }
    LOGD("Java_conex_facerecognition_CVFaceRecognizer_nativeStop exit");
}


JNIEXPORT jboolean JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeTrain
(JNIEnv * jenv, jclass, jlong thiz, jlongArray images, jintArray labels)
{
    bool final = false;
    LOGD("Java_conex_facerecognition_CVFaceRecognizer__nativeTrain enter");
    try {
        size_t objCount = (size_t) (jenv->GetArrayLength(images));
        vector<Mat> c_images;
        vector<int> c_labels;
        for (int i = 0; i < (int) objCount; i++)
        {
            int label = (int)(jenv->GetIntArrayElements(labels,false)[i]);
            c_labels.push_back(label);
            Mat jImage = *((Mat*) (jenv->GetLongArrayElements(images,false)[i]));
            c_images.push_back(jImage);
        }
        FaceRecognizer* model = ((FaceRecognizer*)(thiz));
        string name = model->info()->name();
        model->train(c_images,c_labels);

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

JNIEXPORT jint JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativePredict
        (JNIEnv * jenv, jclass, jlong thiz, jlong image)
{
    int result = -1;
    LOGD("Java_conex_facerecognition_CVFaceRecognizer__nativeTrain enter");
    try {
        Mat jImage = *((Mat*) (image));
        FaceRecognizer* model = ((FaceRecognizer*)(thiz));
        string name = model->info()->name();
        result = model->predict(jImage);
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
    return (jint)result;
}
