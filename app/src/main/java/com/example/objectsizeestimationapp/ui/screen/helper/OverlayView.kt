package com.example.objectsizeestimationapp.ui.screen.helper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import org.tensorflow.lite.task.vision.detector.Detection

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val boxPaint: Paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val textPaint: Paint = Paint().apply {
        color = Color.GREEN
        textSize = 50f
        style = Paint.Style.FILL
    }

    // Hold detection results (the detections should be passed to this view)
    private var results: List<Detection>? = null

    // To scale bounding boxes from model input size to actual image size
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var modelInputWidth: Int = 0
    private var modelInputHeight: Int = 0

    // Set the detection results and image dimensions
    fun setResults(detections: List<Detection>?, imageWidth: Int, imageHeight: Int, modelInputWidth: Int, modelInputHeight: Int) {
        this.results = detections
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        this.modelInputWidth = modelInputWidth
        this.modelInputHeight = modelInputHeight
        invalidate() // Trigger re-draw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // If there are no detection results, do nothing
        results?.forEach { detection ->
            val boundingBox = detection.boundingBox

            // Scale bounding box to match the image dimensions
            val scaledBox = scaleBoundingBoxToImageSize(boundingBox)

            // Draw bounding box on the canvas
            canvas.drawRect(scaledBox, boxPaint)

            // Draw label and confidence score (optional)
            if (detection.categories.isNotEmpty()) {
                val label = detection.categories[0].label
                val confidence = (detection.categories[0].score * 100).toInt()
                canvas.drawText("$label ($confidence%)", scaledBox.left, scaledBox.top - 10f, textPaint)
            }
        }
    }

    private fun scaleBoundingBoxToImageSize(boundingBox: RectF): RectF {
        // Scale the coordinates of the bounding box to match the current image dimensions
        val left = boundingBox.left * imageWidth / modelInputWidth
        val top = boundingBox.top * imageHeight / modelInputHeight
        val right = boundingBox.right * imageWidth / modelInputWidth
        val bottom = boundingBox.bottom * imageHeight / modelInputHeight
        return RectF(left, top, right, bottom)
    }
}
