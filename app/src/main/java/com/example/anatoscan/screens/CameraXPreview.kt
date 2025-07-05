package com.example.anatoscan.screens


import android.graphics.Bitmap
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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.NavController
import com.example.anatoscan.viewmodel.BitmapViewModel

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
        Button(
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
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            Text("Capture")
        }
    }

}
