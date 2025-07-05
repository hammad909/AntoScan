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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
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
                            if (result != null && result.handedness().isNotEmpty()) {
                                val handLabels = result.handedness().mapIndexed { index, hand ->
                                    val side = hand[0].categoryName()  // "Left" or "Right"
                                    "Hand ${index + 1}: $side"
                                }
                                message = "Detected ${handLabels.size} hand(s):\n" + handLabels.joinToString("\n")
                            }
                            detectionResult = message
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
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



//Takes a bitmap image.
//Uses MediaPipe + TensorFlow Lite (hand_landmarker.task model).
//Detects up to 2 hands and their landmarks.
//Returns the detection result to your UI via a callback onResult.
fun analyzeHands(context: Context, bitmap: Bitmap, onResult: (HandLandmarkerResult?) -> Unit) {


   //Creates a builder for configuring the HandLandmarker.
    val options = HandLandmarker.HandLandmarkerOptions.builder()


        .setBaseOptions(
            BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .build()
        )

        //Sets the running mode to IMAGE, meaning the model will process static images (not video or real-time streams).
        .setRunningMode(RunningMode.IMAGE)
        //Tells the model to detect up to 2 hands in the image.
        .setNumHands(2)
        .build()


    //Creates the HandLandmarker instance using the provided options and context.
    //This object is responsible for running hand landmark detection on images.
    val handLandmarker = HandLandmarker.createFromOptions(context, options)

    val mpImage = BitmapImageBuilder(bitmap).build()
    val result = handLandmarker.detect(mpImage)

    onResult(result)

}
