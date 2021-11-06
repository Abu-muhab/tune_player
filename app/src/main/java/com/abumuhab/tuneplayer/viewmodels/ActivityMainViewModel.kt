package com.abumuhab.tuneplayer.viewmodels

import android.app.Application
import android.content.ComponentName
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abumuhab.tuneplayer.services.MediaPlaybackService

class ActivityMainViewModel(application: Application) : ViewModel() {
    lateinit var mediaBrowser: MediaBrowserCompat
    lateinit var mediaController: MediaControllerCompat
    private var connectionCallBack = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaBrowser.sessionToken.also { token ->
                mediaController =
                    MediaControllerCompat(application.applicationContext, token)
                mediaController.registerCallback(object : MediaControllerCompat.Callback() {

                })

            }
        }
    }


    init {
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