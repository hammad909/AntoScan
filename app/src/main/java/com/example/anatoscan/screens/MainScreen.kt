package com.example.anatoscan.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.anatoscan.viewmodel.CameraViewModel
import java.nio.file.WatchEvent

@Composable
fun MainScreen(navController : NavController,cameraViewModel : CameraViewModel){



    //It's needed to access system services and information, including permission status
    val context = LocalContext.current


    val permissionGranted = cameraViewModel.cameraPermissionGranted.collectAsState()


    //asking for permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()


    ) {
        // the return is managed here
        isGranted: Boolean ->
        if (isGranted) {
            cameraViewModel.onPermissionGranted()
        } else {
            cameraViewModel.onPermissionDenied()
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (permissionGranted.value) "Camera ON" else "Camera OFF",
                color = if (permissionGranted.value) Color.Green else Color.Red
            )

            Spacer(modifier = Modifier.width(16.dp))


            Switch(
                checked = permissionGranted.value,
                onCheckedChange = { checked ->
                    if (checked) {
                        val permissionCheck = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        )
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            cameraViewModel.onPermissionGranted()
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    } else {
                        cameraViewModel.onPermissionDenied()
                        Toast.makeText(context, "Camera turned off", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        Button(
            onClick = {
                if (permissionGranted.value) {
                    navController.navigate("cameraScreen")
                } else {
                    Toast.makeText(context, "Please enable camera permission first", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .padding(top = 24.dp)
        ) {
            Text("Start Camera")
        }
    }


}


