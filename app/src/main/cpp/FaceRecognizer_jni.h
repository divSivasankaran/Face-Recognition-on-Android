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
 * Method:    nativeStart
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeStart
  (JNIEnv *, jclass, jlong);

/*
 * Class:     conex_facerecognition_CVFaceRecognizer
 * Method:    nativeStop
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeStop
  (JNIEnv *, jclass, jlong);


/*
 * Class:     conex_facerecognition_CVFaceRecognizer
 * Method:    nativeTrain
 * Signature: (JJJ)V
 */
JNIEXPORT jboolean JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativeTrain
  (JNIEnv *, jclass, jlong, jlongArray , jintArray);

/*
 * Class:     conex_facerecognition_CVFaceRecognizer
 * Method:    nativePredict
 * Signature: (JJJ)V
 */
JNIEXPORT jint JNICALL Java_conex_facerecognition_CVFaceRecognizer_nativePredict
        (JNIEnv *, jclass, jlong, jlong);

#ifdef __cplusplus
}
#endif
#endif
