package com.example.anatoscan.screens

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlin.math.abs
import com.example.anatoscan.ml.PoseOverlayView


@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraXPreview(navController : NavController){

    //It's needed to access system services and information, including permission status
    val context = LocalContext.current

    //happen while navigating services aware
    //localLifeIOwner for the camera on and off check Required by CameraX
    val lifeCycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current


    var lastToastTime = remember { mutableStateOf(0L) }
    var lastPosture = remember { mutableStateOf("") }

    val landmarksState = remember { mutableStateOf<List<PoseLandmark>>(emptyList()) }

    Box(modifier = Modifier.fillMaxSize()) {
        //Allows you to use traditional Android Views (like PreviewView) inside Jetpack Compose.
        AndroidView(factory = { ctx ->

            //This is a View from CameraX that shows the camera preview
            val previewView = PreviewView(ctx)

            //which manages camera lifecycle and binds camera use cases (preview, analysis, image capture, etc.).
            //Asks the system for a camera provider, which gives you access to the device's cameras.
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val poseDetector = PoseDetection.getClient(
                AccuratePoseDetectorOptions.Builder()
                    .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                    .build()
            )



            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val inputImage =
                        InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                    poseDetector.process(inputImage)

                        .addOnSuccessListener { pose ->

                            val allLandmarks = pose.allPoseLandmarks
                            landmarksState.value = allLandmarks

                            val currentTime = System.currentTimeMillis()

                            if (allLandmarks.isEmpty() && lastPosture.value != "No Person") {
                                if (currentTime - lastToastTime.value > 1000) {
                                    Toast.makeText(context, "🚫 No person detected", Toast.LENGTH_SHORT).show()
                                    lastToastTime.value = currentTime
                                    lastPosture.value = "No Person"
                                }
                                return@addOnSuccessListener
                            }

                            val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
                            val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
                            val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

                            if (leftHip != null && leftKnee != null && leftAnkle != null) {
                                val hipY = leftHip.position.y
                                val kneeY = leftKnee.position.y
                                val ankleY = leftAnkle.position.y

                                val hipKneeDiff = abs(hipY - kneeY)
                                val kneeAnkleDiff = abs(kneeY - ankleY)

                                val posture = when {
                                    hipKneeDiff < 50 && kneeAnkleDiff < 50 -> "Prone"
                                    hipY < kneeY && kneeY < ankleY -> "Standing"
                                    hipY < kneeY && abs(kneeY - ankleY) < 80 -> "Sitting"
                                    else -> "Unknown"
                                }

                                if (currentTime - lastToastTime.value > 1000 && posture != lastPosture.value) {
                                    Toast.makeText(context, "🧍 Detected posture: $posture", Toast.LENGTH_SHORT).show()
                                    lastToastTime.value = currentTime
                                    lastPosture.value = posture
                                }


                                println("Detected posture: $posture")
                            }
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }



            cameraProviderFuture.addListener({
                //Once the future is done, you get the actual cameraProvider instance to control the camera.
                val cameraProvider = cameraProviderFuture.get()

                //Preview.Builder() creates a Preview use case — meaning "I want to show live video."
                //setSurfaceProvider(previewView.surfaceProvider) tells CameraX:
                //"Draw the camera feed inside this PreviewView."
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                //Use the back camera.
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


                try {
                    //Before binding new use cases, you unbind any old ones, to avoid conflicts.
                    cameraProvider.unbindAll()

                    //Camera is now bound to your screen’s lifecycle (starts/stops automatically),
                    //Using the back camera,
                    //With the Preview use case, shown in your PreviewView.
                    cameraProvider.bindToLifecycle(
                        lifeCycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }, ContextCompat.getMainExecutor(ctx))

            previewView
        })

        PoseOverlayView(
            landmarks = landmarksState.value,
            viewWidth = 480,
            viewHeight = 640
        )
    }
}

