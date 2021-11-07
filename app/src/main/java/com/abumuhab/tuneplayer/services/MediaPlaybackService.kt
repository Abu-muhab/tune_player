package com.abumuhab.tuneplayer.services


import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import com.abumuhab.tuneplayer.R
import com.abumuhab.tuneplayer.repositories.AudioRepository
import kotlinx.coroutines.*
import java.lang.Exception

class MediaPlaybackService : MediaBrowserServiceCompat() {
    private var mediaSession: MediaSessionCompat? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private val mediaItems = arrayListOf<MediaBrowserCompat.MediaItem>()
    private lateinit var audioRepository: AudioRepository

    override fun onCreate() {
        super.onCreate()

        audioRepository = AudioRepository(application)
        mediaSession = MediaSessionCompat(baseContext, "ALARM_MEDIA_SESSION").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE)

            setPlaybackState(stateBuilder.build())

            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
                    val builder =
                        NotificationCompat.Builder(applicationContext, "alarm").apply {
                            setOngoing(true)
                            setContentTitle("New alarm")
                            setSmallIcon(R.drawable.ic_baseline_favorite_border_24)
                            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        }
                    startForeground(-8000, builder.build())
                    this@MediaPlaybackService.startService(
                        Intent(
                            applicationContext,
                            MediaPlaybackService::class.java
                        )
                    )

                    // stop media player if active before sounding another alarm
                    mediaPlayer?.apply {
                        try {
                            stop()
                            release()
                        } catch (e: Exception) {
                            //do nothing. This just means we attempted to stop the player while nothing was being played
                        }
                    }
                    mediaPlayer = MediaPlayer.create(applicationContext, uri).apply {
                        start()
                        setOnCompletionListener {
                            onSkipToNext()
                        }
                    }

                }

                override fun onStop() {
                    mediaSession
                    mediaPlayer?.apply {
                        stop()
                        release()
                    }
                    this@MediaPlaybackService.stopForeground(true)
                    this@MediaPlaybackService.stopSelf()
                }
            })

            setSessionToken(sessionToken)

            isActive = true
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        if (parentId == "tracks") {
            audioRepository.listAudioFiles().forEach {
                val mediaDescriptionBuilder = MediaDescriptionCompat.Builder()
                    .setMediaId(it.id)
                    .setMediaUri(it.uri)
                    .setTitle(it.name)
                    .setSubtitle(it.artiste)
                mediaItems.add(MediaBrowserCompat.MediaItem(mediaDescriptionBuilder.build(), 0))
            }
            result.sendResult(mediaItems)
        } else {
            result.sendResult(null)
        }
    }
}