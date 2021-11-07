package com.abumuhab.tuneplayer

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.util.Size
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.ImageViewCompat
import androidx.databinding.BindingAdapter
import com.abumuhab.tuneplayer.models.Audio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@BindingAdapter("audioThumbnail")
fun bindImage(imageView: ImageView, audioThumbnail: Audio?) {
    audioThumbnail?.let {
        var thumbnail: Bitmap? = null
        CoroutineScope(Dispatchers.Main.immediate).launch {
            withContext(Dispatchers.IO) {
                try {
                    thumbnail = imageView.context.contentResolver.loadThumbnail(
                        it.uri,
                        Size(200, 200),
                        null
                    )
                } catch (err: Exception) {
                }
            }
            if (thumbnail != null) {
                imageView.setImageBitmap(thumbnail)
                ImageViewCompat.setImageTintList(imageView, null)
            } else {
                imageView.setImageDrawable(
                    AppCompatResources.getDrawable(
                        imageView.context,
                        R.drawable.album
                    )
                )
                ImageViewCompat.setImageTintList(
                    imageView,
                    ColorStateList.valueOf(imageView.context.getColor(R.color.white))
                )
            }
        }
    }
}