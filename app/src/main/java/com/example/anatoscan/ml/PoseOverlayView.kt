package com.example.anatoscan.ml

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.pose.PoseLandmark

@Composable
fun PoseOverlayView(
    landmarks: List<PoseLandmark>,
    viewWidth: Int,
    viewHeight: Int
) {
    val density = LocalDensity.current.density

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / viewWidth.toFloat()
            val scaleY = size.height / viewHeight.toFloat()

            // Draw dots
            landmarks.forEach { landmark ->
                val cx = landmark.position.x * scaleX
                val cy = landmark.position.y * scaleY
                drawCircle(
                    color = Color.Green,
                    radius = 6.dp.toPx(),
                    center = Offset(cx, cy)
                )
            }

            // Draw lines between selected keypoints
            val connections = listOf(
                PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_ELBOW,
                PoseLandmark.LEFT_ELBOW to PoseLandmark.LEFT_WRIST,
                PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_ELBOW,
                PoseLandmark.RIGHT_ELBOW to PoseLandmark.RIGHT_WRIST,
                PoseLandmark.LEFT_HIP to PoseLandmark.LEFT_KNEE,
                PoseLandmark.LEFT_KNEE to PoseLandmark.LEFT_ANKLE,
                PoseLandmark.RIGHT_HIP to PoseLandmark.RIGHT_KNEE,
                PoseLandmark.RIGHT_KNEE to PoseLandmark.RIGHT_ANKLE,
                PoseLandmark.LEFT_SHOULDER to PoseLandmark.RIGHT_SHOULDER,
                PoseLandmark.LEFT_HIP to PoseLandmark.RIGHT_HIP
            )

            connections.forEach { (startIdx, endIdx) ->
                val start = landmarks.find { it.landmarkType == startIdx }
                val end = landmarks.find { it.landmarkType == endIdx }

                if (start != null && end != null) {
                    drawLine(
                        color = Color.Red,
                        strokeWidth = 2.dp.toPx(),
                        start = Offset(start.position.x * scaleX, start.position.y * scaleY),
                        end = Offset(end.position.x * scaleX, end.position.y * scaleY)
                    )
                }
            }
        }
    }
}
