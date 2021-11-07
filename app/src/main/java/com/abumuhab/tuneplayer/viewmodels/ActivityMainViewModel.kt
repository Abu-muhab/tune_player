package com.abumuhab.tuneplayer.viewmodels

import android.app.Application
import android.content.ComponentName
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abumuhab.tuneplayer.models.Audio
import com.abumuhab.tuneplayer.services.MediaPlaybackService
import com.abumuhab.tuneplayer.util.findItemPositionInList

class ActivityMainViewModel(private val application: Application) : ViewModel() {
    lateinit var mediaBrowser: MediaBrowserCompat
    lateinit var mediaController: MediaControllerCompat
    lateinit var connectionCallBack: MediaBrowserCompat.ConnectionCallback
    var queue: MutableList<MediaSessionCompat.QueueItem>? = null

    val showMusicControls = MutableLiveData<Boolean>()
    val nowPlaying = MutableLiveData<Audio>()


    init {
        showMusicControls.value = false
        connectToMediaPlaybackService()
    }

    private fun connectToMediaPlaybackService() {
        connectionCallBack = object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                mediaBrowser.sessionToken.also { token ->
                    mediaController =
                        MediaControllerCompat(application.applicationContext, token)
                    mediaController.registerCallback(object : MediaControllerCompat.Callback() {
                        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                            state?.let {
                                if (queue != null) {
                                    state.activeQueueItemId
                                    val nowPlayingQueuePosition =
                                        findItemPositionInList(queue!!) { queueItem ->
                                            state.activeQueueItemId == queueItem.queueId
                                        }
                                    Log.i(
                                        "QUQUQUQUQUQU",
                                        queue!![nowPlayingQueuePosition].description.title.toString()
                                    )

                                    nowPlaying

                                    if(showMusicControls.value==false){
                                        showMusicControls.value=true
                                    }
                                }
                            }
                        }

                        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
                            this@ActivityMainViewModel.queue = queue
                        }

                    })
                }
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

class ActivityMainViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityMainViewModel::class.java)) {
            return ActivityMainViewModel(application) as T
        }
        throw  IllegalArgumentException("Unknown ViewModel class")
    }
}