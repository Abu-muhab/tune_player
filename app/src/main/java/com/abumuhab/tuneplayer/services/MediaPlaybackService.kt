package com.abumuhab.tuneplayer.services


import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import com.abumuhab.tuneplayer.R
import com.abumuhab.tuneplayer.repositories.AudioRepository
import com.abumuhab.tuneplayer.util.createNotificationChannel
import com.abumuhab.tuneplayer.util.findItemPositionInList
import kotlinx.coroutines.*
import java.lang.Exception

class MediaPlaybackService : MediaBrowserServiceCompat() {
    private var mediaSession: MediaSessionCompat? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private val mediaItems = arrayListOf<MediaBrowserCompat.MediaItem>()
    private lateinit var audioRepository: AudioRepository

    private var currentQueue: MutableList<MediaSessionCompat.QueueItem>? = null
    private var nowPlaying: MediaBrowserCompat.MediaItem? = null

    override fun onCreate() {
        super.onCreate()

        audioRepository = AudioRepository(application)
        mediaSession = MediaSessionCompat(baseContext, "ALARM_MEDIA_SESSION").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE)

            setPlaybackState(stateBuilder.build())

            setCallback(object : MediaSessionCompat.Callback() {
                override fun onSkipToNext() {
                    if (currentQueue != null && nowPlaying != null) {
                        /**
                         * Get the position od the nowPlaying mediaItem on the queue
                         */
                        val activeQueItemPosition =
                            findItemPositionInList(currentQueue!!.toList()) { queueItem ->
                                queueItem.description.mediaId == nowPlaying!!.mediaId
                            }

                        /**
                         * Get the mediaDescription of the next item on the ques and create the mediaItem to be played
                         */
                        val nextMediaItem = MediaBrowserCompat.MediaItem(
                            currentQueue!![activeQueItemPosition + 1].description,
                            0
                        )

                        /**
                         * PLay the nex media item
                         */
                        playMediaItem(nextMediaItem) {
                            onSkipToNext()
                        }
                    }
                }

                override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
                    /**
                     * Start in foreground and display ongoing notification
                     */
                    //TODO: replace placeholder notification
                    createNotificationChannel(applicationContext, "Playback", "Playback")
                    val builder =
                        NotificationCompat.Builder(applicationContext, "Playback").apply {
                            setOngoing(true)
                            setContentTitle("New alarm")
                            setSmallIcon(R.drawable.ic_baseline_favorite_border_24)
                            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        }
                    startForeground(-8000, builder.build())


                    /**
                     * start service so it isn't bound to the UI and can continue playing in the background
                     */
                    this@MediaPlaybackService.startService(
                        Intent(
                            applicationContext,
                            MediaPlaybackService::class.java
                        )
                    )

                    val mediaItem = findMediaItemById(mediaId, mediaItems)
                    mediaItem?.let {
                        /**
                         * Play mediaItem
                         */
                        playMediaItem(mediaItem) {
                            onSkipToNext()
                        }

                        /**
                         * Update playback information
                         */
                        stateBuilder = PlaybackStateCompat.Builder()
                            .setActions(PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                            .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1F)
                            .setActiveQueueItemId(0)
                        setPlaybackState(stateBuilder.build())


                        /**
                         * Generate playback queue
                         */
                        currentQueue = generateQueue(mediaId)
                        setQueue(currentQueue)
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

    fun generateQueue(mediaId: String): MutableList<MediaSessionCompat.QueueItem> {
//        val positionInQueue = findItemPositionInList(mediaItems) { mediaItem ->
//            mediaItem.description.mediaId == mediaId
//        }

        var queueCount = 0L
        val queueItems = mutableListOf<MediaSessionCompat.QueueItem>()
        mediaItems.forEach {
            queueItems.add(MediaSessionCompat.QueueItem(it.description, queueCount))
            queueCount++
        }
        return queueItems.toMutableList()
    }


    fun playMediaItem(mediaItem: MediaBrowserCompat.MediaItem, onFinishedPlaying: () -> Unit) {
        /**
         * Stop mediaPlayer if active before creating a new instance
         */
        mediaPlayer?.apply {
            try {
                stop()
                release()
                nowPlaying = null
            } catch (e: Exception) {
                //do nothing. This just means we attempted to stop the player while nothing was being played
            }
        }

        /**
         * Create new instance of mediaPlayer with the given mediaUri
         */
        mediaPlayer =
            MediaPlayer.create(applicationContext, mediaItem.description.mediaUri)
                .apply {
                    start()
                    nowPlaying = mediaItem
                    setOnCompletionListener {
                        onFinishedPlaying()
                    }
                }
    }


    fun findMediaItemById(
        mediaId: String,
        list: List<MediaBrowserCompat.MediaItem>
    ): MediaBrowserCompat.MediaItem? {
        return list.firstOrNull { it.description.mediaId == mediaId }
    }
}