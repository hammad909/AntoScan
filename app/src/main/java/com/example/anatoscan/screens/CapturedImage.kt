package com.example.anatoscan.screens

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.anatoscan.viewmodel.BitmapViewModel
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult


@Composable
fun CapturedImage(
    navController: NavController,
    bitmapViewModel: BitmapViewModel
) {
    val context = LocalContext.current
    val bitmap = bitmapViewModel.capturedBitmap
    var detectionResult by remember { mutableStateOf<String?>(null) }
    var postureResult by remember { mutableStateOf<String?>(null) } // 🆕 For body pose status

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Captured Image",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                IconButton(
                    onClick = {
                        bitmapViewModel.clearBitmap()
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.7f), shape = MaterialTheme.shapes.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to Camera",
                        tint = Color(0xFF362246)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F0F0))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Analysis Results",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4B2E68),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                detectionResult?.let {
                    Text(
                        text = it,
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
                }

                postureResult?.let {
                    Text(
                        text = it,
                        fontSize = 16.sp,
                        color = Color(0xFF00695C),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
                }
            }

            // 🔁 ROW for Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (bitmap != null) {
                            analyzeHands(context, bitmap) { result ->
                                var message = "No hands detected"
                                if (result != null && result.handednesses().isNotEmpty()) {
                                    val handLabels = result.handednesses().mapIndexed { index, handList ->
                                        val side = handList.firstOrNull()?.categoryName()
                                        if (side != null) "Hand ${index + 1}: $side" else "Hand ${index + 1}: Unknown"
                                    }
                                    message = "Detected ${handLabels.size} hand(s):\n" + handLabels.joinToString("\n")
                                }
                                detectionResult = message
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "No image to analyze!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4B2E68),
                        contentColor = Color.White
                    )
                ) {
                    Text("Analyze Hand", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        if (bitmap != null) {
                            analyzePose(context, bitmap) { poseResult ->
                                if (poseResult != null && poseResult.landmarks().isNotEmpty()) {
                                    val poseLabel = estimatePosture(poseResult)
                                    postureResult = "Posture: $poseLabel"
                                } else {
                                    postureResult = "No person detected"
                                }
                            }
                        } else {
                            Toast.makeText(context, "No image to analyze!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00695C),
                        contentColor = Color.White
                    )
                ) {
                    Text("Analyze Pose", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}



fun analyzeHands(context: Context, bitmap: Bitmap, onResult: (HandLandmarkerResult?) -> Unit) {
    val options = HandLandmarker.HandLandmarkerOptions.builder()
        .setBaseOptions(
            BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .build()
        )
        .setRunningMode(RunningMode.IMAGE)
        .setNumHands(2)
        .build()

    val handLandmarker = HandLandmarker.createFromOptions(context, options)
    val mpImage = BitmapImageBuilder(bitmap).build()
    val result = handLandmarker.detect(mpImage)

    onResult(result)

    handLandmarker.close()

}

fun analyzePose(context: Context, bitmap: Bitmap, onResult: (PoseLandmarkerResult?) -> Unit) {
    val options = PoseLandmarker.PoseLandmarkerOptions.builder()
        .setBaseOptions(
            BaseOptions.builder()
                .setModelAssetPath("pose_landmarker_full.task")
                .build()
        )
        .setRunningMode(RunningMode.IMAGE)
        .setMinPoseDetectionConfidence(0.5f)
        .setMinTrackingConfidence(0.5f)
        .setMinPosePresenceConfidence(0.5f)
        .build()

    val poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    val mpImage = BitmapImageBuilder(bitmap).build()
    val result = poseLandmarker.detect(mpImage)

    onResult(result)

    poseLandmarker.close()
}

fun estimatePosture(result: PoseLandmarkerResult): String {
    val landmarks = result.landmarks().firstOrNull() ?: return "Unknown"

    // Key landmark indices based on MediaPipe pose
    val leftShoulder = landmarks.getOrNull(11)
    val rightShoulder = landmarks.getOrNull(12)
    val leftHip = landmarks.getOrNull(23)
    val rightHip = landmarks.getOrNull(24)
    val leftKnee = landmarks.getOrNull(25)
    val rightKnee = landmarks.getOrNull(26)
    val leftAnkle = landmarks.getOrNull(27)
    val rightAnkle = landmarks.getOrNull(28)

    if (listOf(
            leftShoulder, rightShoulder,
            leftHip, rightHip,
            leftKnee, rightKnee,
            leftAnkle, rightAnkle
        ).any { it == null }) return "Unknown"

    // Average keypoints for vertical estimation
    val shoulderY = (leftShoulder!!.y() + rightShoulder!!.y()) / 2f
    val hipY = (leftHip!!.y() + rightHip!!.y()) / 2f
    val kneeY = (leftKnee!!.y() + rightKnee!!.y()) / 2f
    val ankleY = (leftAnkle!!.y() + rightAnkle!!.y()) / 2f

    // Vertical distances
    val shoulderToHip = hipY - shoulderY
    val hipToKnee = kneeY - hipY
    val kneeToAnkle = ankleY - kneeY
    val totalBodyHeight = ankleY - shoulderY

    // Flat body detection (prone or lying)
    val shoulderHipFlat = kotlin.math.abs(leftShoulder.y() - leftHip.y()) < 0.08f
    val hipKneeFlat = kotlin.math.abs(leftHip.y() - leftKnee.y()) < 0.08f
    val kneeAnkleFlat = kotlin.math.abs(leftKnee.y() - leftAnkle.y()) < 0.08f
    val isLying = (shoulderHipFlat && hipKneeFlat && kneeAnkleFlat) || totalBodyHeight < 0.25f

    if (isLying) return "Prone (Lying)"

    // Normalize segment ratios
    val shoulderToHipRatio = shoulderToHip / totalBodyHeight
    val hipToKneeRatio = hipToKnee / totalBodyHeight
    val kneeToAnkleRatio = kneeToAnkle / totalBodyHeight

    return when {
        // Tall, upright structure
        shoulderToHipRatio > 0.28f && hipToKneeRatio > 0.25f && kneeToAnkleRatio > 0.25f ->
            "Standing"

        // Upper body tall but knees are bent/close to hips
        shoulderToHipRatio > 0.28f && hipToKneeRatio < 0.20f && kneeToAnkleRatio < 0.25f ->
            "Sitting"

        else -> "Unclear"
    }
}


