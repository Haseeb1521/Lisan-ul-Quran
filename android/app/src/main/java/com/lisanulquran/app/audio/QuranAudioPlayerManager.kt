package com.lisanulquran.app.audio

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.lisanulquran.app.data.AppSettings
import com.lisanulquran.app.data.tr

/**
 * Talks to [QuranAudioService]. Holds the player state as Compose state so the
 * mini player / full player / surah screen all update together.
 */
object QuranAudioPlayerManager {
    class Reciter(val name: String, val baseUrl: String)

    val reciters = listOf(
        Reciter("Mishary Rashid Alafasy", "https://everyayah.com/data/Alafasy_128kbps"),
        Reciter("Abdul Rahman Al-Sudais", "https://everyayah.com/data/Abdurrahmaan_As-Sudais_192kbps"),
        Reciter("Maher Al-Muaiqly", "https://everyayah.com/data/MaherAlMuaiqly128kbps"),
        Reciter("Mahmoud Khalil Al-Husary", "https://everyayah.com/data/Husary_128kbps"),
        Reciter("Abdul Basit (Murattal)", "https://everyayah.com/data/Abdul_Basit_Murattal_192kbps"),
        Reciter("Mohamed Al-Minshawi (Murattal)", "https://everyayah.com/data/Minshawy_Murattal_128kbps")
    )

    var hasMedia by mutableStateOf(false)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var isBuffering by mutableStateOf(false)
        private set
    var surahId by mutableIntStateOf(0)
        private set
    var ayahNumber by mutableIntStateOf(0)
        private set
    var surahName by mutableStateOf("")
        private set
    var itemCount by mutableIntStateOf(0)
        private set
    var positionMs by mutableLongStateOf(0L)
        private set
    var durationMs by mutableLongStateOf(0L)
        private set
    var speed by mutableFloatStateOf(1f)
        private set
    var repeatMode by mutableIntStateOf(Player.REPEAT_MODE_OFF)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var controller: MediaController? = null
    private var connecting = false
    private val pending = ArrayList<(MediaController) -> Unit>()
    private var currentCount = 0
    private var currentSingle = false

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            sync()
        }

        override fun onPlayerError(e: PlaybackException) {
            error = tr(
                "Audio nahi chal paya. Internet check karein.",
                "Could not play audio. Please check your internet."
            )
        }
    }

    fun connect(context: Context) {
        if (controller != null || connecting) return
        connecting = true
        val app = context.applicationContext
        val token = SessionToken(app, ComponentName(app, QuranAudioService::class.java))
        val future = MediaController.Builder(app, token).buildAsync()
        future.addListener({
            connecting = false
            try {
                val c = future.get()
                controller = c
                c.addListener(listener)
                sync()
                val todo = pending.toList()
                pending.clear()
                todo.forEach { it(c) }
            } catch (e: Exception) {
                error = e.message
            }
        }, ContextCompat.getMainExecutor(app))
    }

    private fun withController(context: Context, block: (MediaController) -> Unit) {
        val c = controller
        if (c != null) {
            block(c)
        } else {
            pending.add(block)
            connect(context)
        }
    }

    private fun urlFor(surah: Int, ayah: Int): String {
        val r = reciters[AppSettings.reciterIndex.coerceIn(0, reciters.lastIndex)]
        return "${r.baseUrl}/${"%03d".format(surah)}${"%03d".format(ayah)}.mp3"
    }

    /** Plays [surah] from [startAyah] to the end (or only that ayah when [single]). */
    fun play(
        context: Context,
        surah: Int,
        name: String,
        ayahCount: Int,
        startAyah: Int = 1,
        single: Boolean = false
    ) {
        error = null
        currentCount = ayahCount
        currentSingle = single
        val reciter = reciters[AppSettings.reciterIndex.coerceIn(0, reciters.lastIndex)]
        val range = if (single) startAyah..startAyah else 1..ayahCount
        val items = range.map { a ->
            MediaItem.Builder()
                .setMediaId("$surah:$a")
                .setUri(urlFor(surah, a))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("$name • $a")
                        .setArtist(reciter.name)
                        .setAlbumTitle(name)
                        .build()
                )
                .build()
        }
        val startIndex = if (single) 0 else (startAyah - 1).coerceIn(0, items.size - 1)
        withController(context) { c ->
            c.setMediaItems(items, startIndex, 0L)
            c.prepare()
            c.play()
        }
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) {
            c.pause()
        } else {
            if (c.playbackState == Player.STATE_ENDED) c.seekTo(0, 0L)
            c.play()
        }
    }

    fun next() { controller?.seekToNextMediaItem() }

    fun previous() { controller?.seekToPreviousMediaItem() }

    fun seekTo(ms: Long) {
        controller?.seekTo(ms)
        positionMs = ms
    }

    fun setSpeed(value: Float) { controller?.setPlaybackSpeed(value) }

    fun cycleRepeat() {
        val c = controller ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun changeReciter(context: Context, index: Int) {
        AppSettings.setReciter(index)
        if (hasMedia && surahId > 0) {
            play(context, surahId, surahName, currentCount, ayahNumber, currentSingle)
        }
    }

    fun stop() {
        controller?.let {
            it.stop()
            it.clearMediaItems()
        }
        sync()
    }

    fun refreshPosition() {
        val c = controller ?: return
        positionMs = c.currentPosition.coerceAtLeast(0L)
    }

    private fun sync() {
        val c = controller ?: return
        hasMedia = c.mediaItemCount > 0
        isPlaying = c.isPlaying
        isBuffering = c.playbackState == Player.STATE_BUFFERING
        itemCount = c.mediaItemCount
        val item = c.currentMediaItem
        val id = item?.mediaId
        if (id != null) {
            val parts = id.split(":")
            if (parts.size == 2) {
                surahId = parts[0].toIntOrNull() ?: 0
                ayahNumber = parts[1].toIntOrNull() ?: 0
            }
            surahName = item.mediaMetadata.albumTitle?.toString() ?: surahName
        }
        durationMs = if (c.duration == C.TIME_UNSET) 0L else c.duration.coerceAtLeast(0L)
        speed = c.playbackParameters.speed
        repeatMode = c.repeatMode
    }
}
