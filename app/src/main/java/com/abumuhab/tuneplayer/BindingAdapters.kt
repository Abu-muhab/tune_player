package com.abumuhab.tuneplayer

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("imageBitmap")
fun bindImage(imageView: ImageView,imageBitmap: Bitmap?){
    imageBitmap?.let {
        imageView.setImageBitmap(imageBitmap)
    }
}