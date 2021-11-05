package com.abumuhab.tuneplayer.models

import android.graphics.Bitmap
import android.net.Uri

data class Audio(val name: String, val id: Long, var uri: Uri, var thumbnail: Bitmap? = null)