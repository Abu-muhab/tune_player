package com.abumuhab.tuneplayer.viewmodels

import android.app.Application
import android.content.ComponentName
import android.media.session.PlaybackState
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
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

    val nowPlaying = MutableLiveData<Audio>()

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
                                override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                                    if (audios.value?.isNotEmpty() == true) {
                                        val bundle = state!!.extras
                                        val audio = Audio(
                                            bundle!!.getString("title").toString(),
                                            bundle.getString("id").toString(),
                                            Uri.parse(bundle.getString("uri").toString()),
                                            bundle.getString("subtitle").toString()
                                        )
                                        if ((state.playbackState as PlaybackState).state == PlaybackStateCompat.STATE_PLAYING) {
                                            audio.isPlaying = true
                                        }
                                        nowPlaying.value=audio

//                                        val audios = audios.value!!.toMutableList()
//                                        audios.forEach {
//                                            if (it.name == audio.name) {
//                                                it.nowPlaying = true
//                                                if ((state.playbackState as PlaybackState).state == PlaybackStateCompat.STATE_PLAYING) {
//                                                    it.isPlaying = true
//                                                }
//                                            } else {
//                                                it.nowPlaying = false
//                                                it.isPlaying = false
//                                            }
//                                        }

//                                        this@TracksViewModel.audios.value = audios
                                    }
                                }
                            })
                        }
                    mediaController.value = _mediaController
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