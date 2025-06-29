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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
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
        }
    }

    Column(verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()){


        //button to handle the permission
        Button(onClick = {

            //self check for permission is it granted or not
            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            if(permissionCheck == PackageManager.PERMISSION_GRANTED){
                cameraViewModel.onPermissionGranted()

                if(permissionGranted.value){
                   navController.navigate("cameraScreen")
                }else{
                    Toast.makeText(context, "Permission failed", Toast.LENGTH_SHORT).show()
                }

            }else{
                //if its not granted do this(ask for permission)
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }

        }) {

            Text("Identify")

        }


    }



}

