package com.example.anatoscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.anatoscan.ui.theme.AnatoScanTheme
import com.example.anatoscan.viewmodel.CameraViewModel
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import com.example.anatoscan.appNavigation.MyAppNavigation
import com.example.anatoscan.viewmodel.BitmapViewModel

class MainActivity : ComponentActivity() {

    val cameraViewModel: CameraViewModel by viewModels()
    val bitmapViewModel : BitmapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        setContent {

            AnatoScanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation(
                        modifier = Modifier.padding(innerPadding),
                           cameraViewModel,
                           bitmapViewModel
                    )


                }
            }
        }
    }
}