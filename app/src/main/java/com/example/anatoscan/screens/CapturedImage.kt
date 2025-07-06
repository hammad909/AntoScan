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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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


@Composable
fun CapturedImage(
    navController: NavController,
    bitmapViewModel: BitmapViewModel
) {
    val context = LocalContext.current
    val bitmap = bitmapViewModel.capturedBitmap
    var detectionResult by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Captured Image",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                )
            }

            detectionResult?.let { result ->
                Text(
                    text = result,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Analyze Button - full width and styled
            Button(
                onClick = {
                    if (bitmap != null) {
                        analyzeHands(context, bitmap) { result ->
                            var message = "No hands detected"
                            // Check if result is not null and has detected hands
                            // IMPORTANT CHANGE: Use .handednesses() (plural)
                            if (result != null && result.handednesses().isNotEmpty()) {
                                val handLabels = result.handednesses().mapIndexed { index, handList -> //  handList is List<Category> for one hand
                                    // Get the most confident category (Left/Right) for this hand
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4B2E68),
                    contentColor = Color.White
                )
            ) {
                Text("Analyze Hand")
            }

            // Back Button - icon only
            IconButton(
                onClick = {
                    bitmapViewModel.clearBitmap()
                    navController.popBackStack()
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, shape = MaterialTheme.shapes.small)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to Camera",
                    tint = Color(0xFF362246)
                )
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


