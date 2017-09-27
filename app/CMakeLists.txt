cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#file(GLOB SRC_FILES
#    src/main/cpp/*.h
#    src/main/cpp/*.cpp
#)
set(OpenCV_STATIC ON)
set(OpenCV_DIR C:/OpenCV-android-sdk/sdk/native/jni)
find_package (OpenCV REQUIRED)
message(STATUS "OpenCV libraries: ${OpenCV_LIBS}")
add_library( native-lib
             SHARED
             #$SRC_FILES$
             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp
             src/main/cpp/FaceTracker_jni.h
             src/main/cpp/FaceTracker_jni.cpp
             src/main/cpp/FaceRecognizer_jni.h
             src/main/cpp/FaceRecognizer_jni.cpp)

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )


target_link_libraries( # Specifies the target library.
                       native-lib
                       ${OpenCV_LIBS}
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )