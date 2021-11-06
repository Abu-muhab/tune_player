package com.abumuhab.tuneplayer.models

import android.graphics.Bitmap
import android.net.Uri

data class Audio(
    val name: String,
    val id: String,
    var uri: Uri,
    val artiste: String,
    var thumbnail: Bitmap? = null
)