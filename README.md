# Build Challenges

While it was easy to obtain the library files, including them in the application turned out to be difficult: After including OpenCV as a module to Android Studio, we had to adapt the gradle buildfiles to match our app and build/target version. There was a known problem in the buildfile, where  ```apply plugin: 'com.android.application'``` was declared instead of ```apply plugin: 'com.android.library'```. After fixing this and rebuilding everything, we needed to include the library files in our directory structure and point gradle to the native libraries. We then included the libraries using ```System.loadLibrary("opencv_java4");``` in our class.

We struggled to load OpenCV, though. We expected ```OpenCVLoader.initAsync()``` to notice our imported libraries. After literally HOURS of research we found out that ```initAsync()``` just does not work with native libraries. We had to use the blocking ```OpenCVLoader.initDebug()``` method.



# Nose Size Calculation

The classifier returns rectangle shapes around faces. We therefore used the provided size of the face (aka the rectangle) to calculate the size: 10% of the width of a face seemed to be an adequate size for all distances.
