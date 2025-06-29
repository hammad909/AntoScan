package com.example.anatoscan.screens

import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController


@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraXPreview(navController : NavController){

    //It's needed to access system services and information, including permission status
    val context = LocalContext.current

    //happen while navigating services aware
    //localLifeIOwner for the camera on and off check Required by CameraX
    val lifeCycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    //Allows you to use traditional Android Views (like PreviewView) inside Jetpack Compose.
    AndroidView(factory = { ctx ->

        //This is a View from CameraX that shows the camera preview
        val previewView = PreviewView(ctx)

        //which manages camera lifecycle and binds camera use cases (preview, analysis, image capture, etc.).
        //Asks the system for a camera provider, which gives you access to the device's cameras.
        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

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
                cameraProvider.bindToLifecycle(lifeCycleOwner, cameraSelector, preview)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(ctx))

        previewView
    })


}

