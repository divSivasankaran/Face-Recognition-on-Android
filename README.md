## Project Theia - Face Recognition on Android

This is an android app that can use an inbuilt camera/video stream via Wifi/Wifi-Direct to recognize people from a pre-enrolled list of persons using Face Recognition. The goal is to be able to run the algorithms locally on the device without accessing API/servers running on the cloud & utilize only a single photo per enrollment. To make it clear, the purpose was to just demonstrate and quickly integrate existing open-source Face APIs as a starter kit for further work.

For face recognition on the devices, the following libraries have been used/integrated:

 * [OpenCV FaceRecognition](https://docs.opencv.org/2.4/modules/contrib/doc/facerec/facerec_tutorial.html#face-recognition-with-opencv)

 * [Facenet on Tensorflow](https://github.com/davidsandberg/facenet)

## [Try it](https://drive.google.com/open?id=1wZIlJuSOf8ZBC93nQdwiwzVRaIp73ULt)

Download the apk file from [here](https://drive.google.com/open?id=1wZIlJuSOf8ZBC93nQdwiwzVRaIp73ULt).

This apk file should work for the architectures mentioned below with Android versions >= Marshmellow (API 23+). *This is the minimum requirement for facenet.*

*Note:* You will have to allow installation from unknown sources option to install the app. I personally don't see any value is putting it out as a stand-alone app on playstore, as it is not a complete solution. Let me know if you think otherwise, and I'll consider putting it up.

## Supported architectures:

The OpenCV version limits the arch support to the following:
* armeabi
* armeabi-v7a
* mips
* x86

Upgrading to OpenCV 3.0+ gives you flexibility to deploy on most architectures, but the java api for face-recognition still has issues and hence I am sticking with OpenCV 2.4 for now.

Lower-end phones running on ARM Cortex(arm64-v8a) will have to use OpenCV 3.0+

## Setup
Versions & Reqs to duplicate build:
* Android studio: `3.0.1`
* Gradle: `3.0.1` (if you aren't using this version, please upgrade via Android Studio & the project should build without any errors)
* OpenCV Android SDK (for NDK Support): `2.4.13.3`

Follow these instructions if you do not have OpenCV set up in your local machine.
* Download [here](http://sourceforge.net/projects/opencvlibrary/files/opencv-android/2.4.11/OpenCV-2.4.11-android-sdk.zip/download)
* Instructions [here](https://docs.opencv.org/2.4/doc/tutorials/introduction/android_binary_package/O4A_SDK.html)

Update `OpenCV_Dir` to reflect the path to your OpenCV Directory in CMakeLists.txt

Verify if these files exist in the project. If not, follow the instructions below to add them.

* `Face-Recognition-on-Android\app\src\main\assets\facenet_inception.pb`
* `Face-Recognition-on-Android\app\src\main\res\raw\haarcascade_frontalface_default.xml`
* `Face-Recognition-on-Android\app\src\main\res\raw\lbpcascade_frontalface.xml`

**Model files**

* The pre-trained weights file can be found [here](https://drive.google.com/file/d/1pp0DGJTLvc93zdr3V80FJ7J5EvOLApE6/view). Add the weights file to the assets folder for the facenet method to work.

* OpenCV Face Tracker requires the cascade files, which are already part of this repository. If you want to test with their other defaults, you can find them [here](https://github.com/opencv/opencv/tree/master/data).

*Note:* The facenet model is not currently optimized for mobile and so the performance is less than ideal right now. I'm working on moving to Tensorflow Lite & will publish a new model soon!

#### Contact
If you are trying to adapt it for your own use case/facing issues with deploying or wish to share your comments/feedback, reach me at <div1090@gmail.com>

#### [MIT License](LICENSE)
