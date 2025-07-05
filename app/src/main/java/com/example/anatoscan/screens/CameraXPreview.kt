package com.example.anatoscan.screens


import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.anatoscan.viewmodel.BitmapViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.FloatingActionButton


@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraXPreview(navController: NavController, bitmapViewModel: BitmapViewModel) {

/* context: Needed for camera and toast messages
 lifecycleOwner: Required to bind CameraX to the lifecycle of the screen*/
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

   /* Creates the PreviewView for CameraX to show the live camera feed
    remember keeps it alive across recompositions*/
    val previewView = remember { PreviewView(context) }


    // State to hold the ImageCapture instance
    val imageCapture = remember { ImageCapture.Builder().build() }

    // Set up the camera
    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Preview takes full screen
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Button overlays the preview, aligned to bottom center
        FloatingActionButton(
            onClick = {
                imageCapture.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val bitmap = image.toBitmap()
                            bitmapViewModel.setBitmap(bitmap)
                            image.close()
                            navController.navigate("result")
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Toast.makeText(context, "Capture failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            },
            containerColor = Color.White,
            contentColor = Color(0xFF362246),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Camera, contentDescription = "Capture Image")
        }

    }

}
