package com.abumuhab.tuneplayer.viewmodels

import android.app.Application
import android.content.ComponentName
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abumuhab.tuneplayer.models.Audio
import com.abumuhab.tuneplayer.services.MediaPlaybackService

class TracksViewModel(private val application: Application) : ViewModel() {
    lateinit var mediaBrowser: MediaBrowserCompat
    private var _mediaController: MediaControllerCompat? = null
    var mediaController = MutableLiveData<MediaControllerCompat>()
    private lateinit var connectionCallBack: MediaBrowserCompat.ConnectionCallback


    val audios = MutableLiveData<List<Audio>>()

    init {
        connectToMediaPlaybackService()
    }

    private fun connectToMediaPlaybackService() {
        connectionCallBack = object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                mediaBrowser.sessionToken.also { token ->
                    _mediaController =
                        MediaControllerCompat(application.applicationContext, token).apply {
                            registerCallback(object : MediaControllerCompat.Callback() {

                            })
                        }
                    mediaController.value=_mediaController
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