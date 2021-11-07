package com.abumuhab.tuneplayer.viewmodels

import android.app.Application
import android.content.ComponentName
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abumuhab.tuneplayer.models.Audio
import com.abumuhab.tuneplayer.repositories.AudioRepository
import com.abumuhab.tuneplayer.services.MediaPlaybackService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class TracksViewModel(private val application: Application) : ViewModel() {
    lateinit var mediaBrowser: MediaBrowserCompat
    lateinit var mediaController: MediaControllerCompat
    private lateinit var connectionCallBack: MediaBrowserCompat.ConnectionCallback

    val audios = MutableLiveData<List<Audio>>()

    init {
        getAudioFiles()
    }

    private fun getAudioFiles() {
        connectionCallBack = object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                mediaBrowser.sessionToken.also { token ->
                    mediaController =
                        MediaControllerCompat(application.applicationContext, token)
                    mediaController.registerCallback(object : MediaControllerCompat.Callback() {

                    })

                }
                mediaBrowser.subscribe(
                    "tracks",
                    object : MediaBrowserCompat.SubscriptionCallback() {
                        override fun onChildrenLoaded(
                            parentId: String,
                            children: MutableList<MediaBrowserCompat.MediaItem>
                        ) {
                            audios.value = children.map {
                                Audio(
                                    it.description.title.toString(),
                                    it.description.mediaId!!,
                                    it.description.mediaUri!!,
                                    it.description.subtitle.toString()
                                )
                            }
                        }
                    })
            }
        }
        mediaBrowser = MediaBrowserCompat(
            application.applicationContext,
            ComponentName(application.applicationContext, MediaPlaybackService::class.java),
            connectionCallBack,
            null
        )
        mediaBrowser.connect()
    }
}

class NowPLayingViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TracksViewModel::class.java)) {
            return TracksViewModel(application) as T
        }
        throw  IllegalArgumentException("Unknown ViewModel class")
    }
}