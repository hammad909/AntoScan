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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    // Check if the bitmap is available (not null)
                    if (bitmap != null) {

                        // Analyze the bitmap for hands
                        analyzeHands(context, bitmap) { result ->

                            // Default message
                            var message = "No hands detected"

                            // If hands are detected, build a message with details
                            if (result != null && result.handedness().isNotEmpty()) {
                                val handLabels = result.handedness().mapIndexed { index, hand ->
                                    val side = hand[0].categoryName()  // "Left" or "Right"
                                    "Hand ${index + 1}: $side"
                                }

                                message = "Detected ${handLabels.size} hand(s):\n" + handLabels.joinToString("\n")
                            }

                            // Show result in a Toast and store it in a variable
                            detectionResult = message
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Analyze Hand")
            }


            Button(
                onClick = {
                    bitmapViewModel.clearBitmap()
                    navController.popBackStack()
                },
            ) {
                Text("Back to Camera")
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
