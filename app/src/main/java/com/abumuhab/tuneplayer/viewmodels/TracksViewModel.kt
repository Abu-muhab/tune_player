package com.abumuhab.tuneplayer.viewmodels

import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.media.session.PlaybackState
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abumuhab.tuneplayer.models.Audio
import com.abumuhab.tuneplayer.services.MediaPlaybackService

class TracksViewModel(
    private val application: Application,
    private val requestPermissionLauncher: ActivityResultLauncher<String>
) : ViewModel() {
    lateinit var mediaBrowser: MediaBrowserCompat
    private var _mediaController: MediaControllerCompat? = null
    var mediaController = MutableLiveData<MediaControllerCompat>()
    private lateinit var connectionCallBack: MediaBrowserCompat.ConnectionCallback

    val storagePermissionGranted = MutableLiveData<Boolean>()

    val audios = MutableLiveData<List<Audio>>()
    val nowPlaying = MutableLiveData<Audio>()

    init {
        storagePermissionGranted.value = false
        checkPermission()
    }

    fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onPermissionGranted()
        } else {
            Log.i("LAUNCHHHH","LLLLLL")
            requestPermissionLauncher.launch(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

    }

    fun onPermissionGranted() {
        storagePermissionGranted.value = true
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
                                        nowPlaying.value = audio
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

class NowPLayingViewModelFactory(
    private val application: Application,
    private val requestPermissionLauncher: ActivityResultLauncher<String>
) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TracksViewModel::class.java)) {
            return TracksViewModel(application, requestPermissionLauncher) as T
        }
        throw  IllegalArgumentException("Unknown ViewModel class")
    }
}