_Object Size Estimation App_
This Android application estimates the size of objects using the device’s camera and a known reference object. The app integrates CameraX and TensorFlow Lite with the SSD MobileNet v1 model for object detection. It also features an OverlayView for visualizing object detection results and a MainViewModel for managing state and business logic.

** Features

* Real-time object detection with TensorFlow Lite (SSD MobileNet v1)
* CameraX integration for capturing images
* Overlay for drawing bounding boxes
* Object size estimation based on a reference object
* Data persistence for detected objects


** Setup Instructions ** 

* Prerequisites
* Ensure you have the following installed:

* Android Studio (latest version recommended)
Android SDK (API 26+)
Gradle 7.0+
Device with CameraX support

* Clone the Repository

git clone // TODO 
cd ObjectSizeEstimationApp

- Open in Android Studio
- Open Android Studio.
- Select Open an Existing Project and navigate to the cloned directory.
- Let Gradle sync automatically.
- Build and Run
- Connect an Android device (or use an emulator with camera support).
- Click Run ▶️ in Android Studio.
- Grant camera permissions when prompted.

** Usage Guide ** 

MainActivity
The app opens with the camera view.
The TensorFlow Lite model detects objects in real-time.
A reference object must be placed in the frame for accurate size estimation.

--OverlayView--
The OverlayView is responsible for drawing detected object bounding boxes.

Location: com.example.objectsizeestimationapp.ui.screen.helper.OverlayView

Fixes an InflateException: Ensure the correct package is used in activity_main.xml:

<com.example.objectsizeestimationapp.ui.screen.helper.OverlayView
android:id="@+id/overlayView"
android:layout_width="match_parent"
android:layout_height="match_parent" />

Initialization in Activity: Ensure OverlayView is properly linked in MainActivity.kt.

--MainViewModel--

MainViewModel handles data processing and business logic, including:
Managing object detection results.
Handling user interactions.
Storing object size estimates.

** Assumptions & Limitations **

Assumptions

A known reference object is placed in the scene.
Camera permissions are granted at runtime.
TensorFlow Lite model is optimized for mobile devices.

Limitations

Detection Accuracy: SSD MobileNet v1 has limited accuracy
Device Dependency: Requires CameraX-supported devices for best performance.
Lighting Conditions: Detection performance varies in low-light environments.

Future Enhancements
✅ Improve object detection with a more advanced model.