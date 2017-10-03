#include <jni.h>
/* Header for class conex_facerecognition_CVFaceRecognizer */

#ifndef _Included_conex_facerecognition_CVFaceRecognizer
#define _Included_conex_facerecognition_CVFaceRecognizer
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     conex_facerecognition_CVFaceRecognizer
 * Method:    nativeCreateObject
 * Signature: (Ljava/lang/String;F)J
 */
JNIEXPORT jlong JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeCreateObject
  (JNIEnv *, jclass);

/*
 * Class:     conex_facerecognition_CVFaceRecognizer
 * Method:    nativeDestroyObject
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeDestroyObject
  (JNIEnv *, jclass, jlong);

/*
 * Class:     conex_facerecognition_CVFaceRecognizer
 * Method:    nativeSave
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeSave
  (JNIEnv *, jclass, jlong, jstring);

/*
 * Class:     conex_facerecognition_CVFaceRecognizer
 * Method:    nativeLoad
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeLoad
  (JNIEnv *, jclass, jlong, jstring);


/*
 * Class:     conex_facerecognition_CVFaceRecognizer
 * Method:    nativeTrain
 * Signature: (JJJ)V
 */
JNIEXPORT jboolean JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeTrain
  (JNIEnv *, jclass, jlong, jlongArray , jobjectArray, jstring);

/*
 * Class:     conex_facerecognition_CVFaceRecognizer
 * Method:    nativePredict
 * Signature: (JJJ)V
 */
JNIEXPORT jstring JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativePredict
        (JNIEnv *, jclass, jlong, jlong, jlong);


/*
 * Class:     conex_facerecognition_CVFaceRecognizer
 * Method:    nativeUpdate
 * Signature: (JJJ)V
 */
JNIEXPORT jboolean JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeUpdate
        (JNIEnv *, jclass, jlong, jlongArray, jobjectArray, jstring);


#ifdef __cplusplus
}
#endif
#endif
