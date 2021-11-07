package com.abumuhab.tuneplayer.models

import android.graphics.Bitmap
import android.net.Uri
import java.lang.Exception

data class Audio(
    val name: String,
    val id: String,
    var uri: Uri,
    val artiste: String
)