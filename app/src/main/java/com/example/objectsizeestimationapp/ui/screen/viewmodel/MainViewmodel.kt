package com.example.objectsizeestimationapp.ui.screen.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.objectsizeestimationapp.ui.screen.helper.OverlayView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _cameraProvider = MutableLiveData<ProcessCameraProvider?>()
    private val cameraProvider: LiveData<ProcessCameraProvider?> = _cameraProvider

    private var objectDetector: ObjectDetector
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        objectDetector = loadModel(application)
        setupCameraProvider(application)
    }

    private fun setupCameraProvider(context: Context) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                _cameraProvider.postValue(cameraProviderFuture.get()) // Ensure it's retrieved safely
            } catch (e: Exception) {
                Log.e("CameraX", "Failed to get camera provider", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun startCamera(
        lifecycleOwner: LifecycleOwner, previewView: PreviewView, overlayView: OverlayView
    ) {
        cameraProvider.observeForever { provider ->
            provider?.let {
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider // Ensure this is set
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImage(imageProxy, overlayView)
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalyzer)
                } catch (e: Exception) {
                    Log.e("CameraX", "Failed to bind use cases", e)
                }
            }
        }
    }

    private fun processImage(imageProxy: ImageProxy, overlayView: OverlayView) {
        viewModelScope.launch(Dispatchers.Main) { // Ensure processing happens on the main thread
            try {
                val bitmap = imageProxy.toImageBitmap()
                val modelInputWidth = 300
                val modelInputHeight = 300
                val resizedBitmap = bitmap.scale(modelInputWidth, modelInputHeight)
                val tensorImage = TensorImage.fromBitmap(resizedBitmap)
                val results = objectDetector.detect(tensorImage)

                val imageWidth = bitmap.width
                val imageHeight = bitmap.height

                overlayView.setResults(
                    results,
                    imageWidth,
                    imageHeight,
                    modelInputWidth,
                    modelInputHeight
                )
                overlayView.invalidate()
            } catch (e: Exception) {
                Log.e("CameraX", "Image processing failed", e)
            } finally {
                imageProxy.close()
            }
        }
    }

    private fun loadModel(context: Context): ObjectDetector {
        val modelPath = "ssd_mobilenet_v1.tflite"
        return try {
            val options = ObjectDetector.ObjectDetectorOptions.builder().setMaxResults(5)
                .setScoreThreshold(0.5f).build()
            ObjectDetector.createFromFileAndOptions(context, modelPath, options)
        } catch (e: Exception) {
            Log.e("TFLite", "Model loading failed", e)
            throw RuntimeException("TensorFlow Lite model file missing")
        }
    }

    private fun ImageProxy.toImageBitmap(): Bitmap {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage =
            android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, width, height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, null, false)
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }
}