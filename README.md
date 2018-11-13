## Project Theia - Face Recognition on Android 

This is an android app that can use an inbuilt camera/video stream via Wifi/Wifi-Direct to recognize people from a pre-enrolled list of persons using Face Recognition. The goal is to be able to run the algorithms locally on the device without accessing API/servers running on the cloud & utilize only a single photo per enrollment.

For face recognition on the devices, the following libraries have been used/integrated:
 
 * [OpenCV FaceRecognition](https://docs.opencv.org/2.4/modules/contrib/doc/facerec/facerec_tutorial.html#face-recognition-with-opencv)
 
 * [Facenet on Tensorflow](https://github.com/davidsandberg/facenet)

## Instructions

The pre-trained facenet weights file can be found [here](https://drive.google.com/file/d/1pp0DGJTLvc93zdr3V80FJ7J5EvOLApE6/view). Add the weights file to the assets folder for the facenet method to work.

Note: The facenet model is not currently optimized for mobile and so the performance is less than ideal right now. I'm working on moving to Tensorflow Lite & will publish a new model soon!

## Work-In-Progress
Facenet on tensorflow-lite - get the converted model [here](https://drive.google.com/open?id=1ClJkipPrEY2sbVc_j2eZrXvokKBh3oHj)
The notebook used to convert the model [here](https://colab.research.google.com/drive/1JwDUAPYdR_jL2-YQeqpaCO3d0XN2Ar84)

**Coming soon** Upgrade the app to use tflite instead. 

#### Contact
If you are trying to adapt it for your own use case/facing issues with deploying or wish to share your comments/feedback, reach me at <div1090@gmail.com>

#### [MIT License](LICENSE)
