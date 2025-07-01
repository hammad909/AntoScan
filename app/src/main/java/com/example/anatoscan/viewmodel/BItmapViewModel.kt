package com.example.anatoscan.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class BitmapViewModel : ViewModel() {

     var capturedBitmap by mutableStateOf<Bitmap?>(null)
     private set

    fun setBitmap(bitmap: Bitmap) {
        capturedBitmap = bitmap
    }

    fun clearBitmap() {
        capturedBitmap = null
    }

}