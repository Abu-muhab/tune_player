package com.abumuhab.tuneplayer.repositories

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import com.abumuhab.tuneplayer.models.Audio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioRepository(private val application: Application) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun listAudioFiles() = channelFlow {
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Artists.ARTIST
        )

        val selection = "${MediaStore.Audio.Media.MIME_TYPE} = ?"
        val selectionArgs = arrayOf("audio/mpeg")
        val sortOrder = MediaStore.Audio.Media.DISPLAY_NAME


        val query =
            application.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

        val audios = arrayListOf<Audio>()

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

            /**
             * iterate through query to get details of each audio retrieved
             */
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                audios.add(Audio(name, id, contentUri))
            }
        }

        /**
         * launch several coroutines to get thumbnails from file for each audio object in parallel
         */
        audios.forEach {
            launch {
                withContext(Dispatchers.IO) {
                    try {
                        it.thumbnail = application.contentResolver.loadThumbnail(
                            it.uri,
                            Size(200, 200),
                            null
                        )
                    } catch (err: Exception) {
                        //do nothing
                    }
                }
                send(it)
            }
        }
    }
}