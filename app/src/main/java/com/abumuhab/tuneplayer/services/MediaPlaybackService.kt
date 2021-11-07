package com.abumuhab.tuneplayer.services


import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
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
    private var activeQueueItemId: Long? = null
    private var nowPlaying: MediaBrowserCompat.MediaItem? = null

    private val musicPlaybackServiceScope = CoroutineScope(Dispatchers.Main)
    private var mediaPlayBackStateUpdateJob: Job? = null

    override fun onCreate() {
        super.onCreate()

        audioRepository = AudioRepository(application)
        mediaSession = MediaSessionCompat(baseContext, "ALARM_MEDIA_SESSION").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE)

            setPlaybackState(stateBuilder.build())

            setCallback(object : MediaSessionCompat.Callback() {
                override fun onSkipToPrevious() {
                    if (currentQueue != null && nowPlaying != null) {
                        /**
                         * Get the position of the nowPlaying mediaItem on the queue
                         */
                        val activeQueItemPosition =
                            findItemPositionInList(currentQueue!!.toList()) { queueItem ->
                                queueItem.description.mediaId == nowPlaying!!.mediaId
                            }


                        /**
                         * if active queue position is first on queue, do nothing
                         */
                        if (activeQueItemPosition == 0) {
                            return
                        }


                        /**
                         * Get the mediaDescription of the next item on the ques and create the mediaItem to be played
                         */
                        val nextMediaItem = MediaBrowserCompat.MediaItem(
                            currentQueue!![activeQueItemPosition - 1].description,
                            0
                        )

                        /**
                         * PLay the nex media item
                         */
                        playMediaItem(nextMediaItem) {
                            onSkipToNext()
                        }
                        activeQueueItemId = activeQueItemPosition - 1L
                    }
                }


                override fun onSkipToNext() {
                    if (currentQueue != null && nowPlaying != null) {
                        /**
                         * Get the position of the nowPlaying mediaItem on the queue
                         */
                        val activeQueItemPosition =
                            findItemPositionInList(currentQueue!!.toList()) { queueItem ->
                                queueItem.description.mediaId == nowPlaying!!.mediaId
                            }


                        /**
                         * if active queue position is last on queue, do nothing
                         */
                        if (activeQueItemPosition == currentQueue!!.size - 1) {
                            return
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
                        activeQueueItemId = activeQueItemPosition + 1L
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
                            setContentTitle("Now Playing")
                            setSmallIcon(R.drawable.ic_baseline_music_note_24)
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
                         * Generate playback queue
                         */
                        currentQueue = generateQueue(mediaId)
                        activeQueueItemId =
                            findItemPositionInList(currentQueue!!.toList()) { queueItem ->
                                queueItem.description.mediaId == nowPlaying!!.mediaId
                            }.toLong()
                        setQueue(currentQueue)
                    }

                }

                override fun onPause() {
                    mediaPlayer?.let {
                        if (it.isPlaying) {
                            it.pause()
                        }
                    }
                }

                override fun onPlay() {
                    mediaPlayer?.let {
                        if (!it.isPlaying) {
                            it.start()
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

    private fun startPlaybackStateUpdateJob() {
        mediaPlayBackStateUpdateJob?.let {
            if (it.isActive) {
                return
            } else if (it.isCompleted) {
                it.start()
                return
            }
        }
        mediaPlayBackStateUpdateJob = musicPlaybackServiceScope.launch {
            while (true) {
                try {
                    if (mediaPlayer!!.isPlaying) {
                        stateBuilder.setActiveQueueItemId(activeQueueItemId!!).setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            mediaPlayer!!.currentPosition.toLong(),
                            1F,
                            1
                        ).setActions(
                            PlaybackStateCompat.ACTION_PAUSE or
                                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        )
                    } else {
                        stateBuilder.setActiveQueueItemId(activeQueueItemId!!).setState(
                            PlaybackStateCompat.STATE_PAUSED,
                            mediaPlayer!!.currentPosition.toLong(),
                            1F,
                            1
                        ).setActions(
                            PlaybackStateCompat.ACTION_PLAY or
                                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        )
                    }

                    val bundle = Bundle()
                    bundle.putString("title", nowPlaying!!.description.title.toString())
                    bundle.putString("subtitle", nowPlaying!!.description.subtitle.toString())
                    bundle.putString("id", nowPlaying!!.description.mediaId)
                    bundle.putString("uri", nowPlaying!!.description.mediaUri.toString())
                    bundle.putInt("duration", mediaPlayer!!.duration)

                    stateBuilder.setExtras(bundle)
                    mediaSession?.setPlaybackState(stateBuilder.build())
                    delay(500)
                } catch (e: Exception) {
                }

                if (mediaSession == null || mediaPlayer == null || nowPlaying == null) {
                    break
                }
            }
            mediaPlayBackStateUpdateJob?.cancel()
        }
    }

    fun stopPlaybackStateUpdateJob() {
        mediaPlayBackStateUpdateJob?.cancel()
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
//                val bundle = Bundle()
//                val bun
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

        /**
         * Start the playbackStateUpdateJob to send playback updates to listeners while playing
         */
        startPlaybackStateUpdateJob()
    }


    fun findMediaItemById(
        mediaId: String,
        list: List<MediaBrowserCompat.MediaItem>
    ): MediaBrowserCompat.MediaItem? {
        return list.firstOrNull { it.description.mediaId == mediaId }
    }
}