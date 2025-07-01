package com.example.anatoscan.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.anatoscan.viewmodel.BitmapViewModel
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CapturedImage(
    navController: NavController,
   bitmapViewModel: BitmapViewModel
) {
    val bitmap = bitmapViewModel.capturedBitmap
    Column {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Captured Image",
                modifier = Modifier.fillMaxSize()
            )
        }

        Button(
            onClick = {
                bitmapViewModel.clearBitmap()
                navController.popBackStack() // Go back to camera screen
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Back to Camera")
        }
    }
}
