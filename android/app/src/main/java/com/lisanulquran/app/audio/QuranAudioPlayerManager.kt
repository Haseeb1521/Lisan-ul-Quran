package com.lisanulquran.app.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class QuranAudioPlayerManager(private val context: Context) {
    val reciters = listOf(
        "Mishary Rashid Alafasy" to "https://everyayah.com/data/Alafasy_128kbps",
        "Abdul Rahman Al-Sudais" to "https://everyayah.com/data/Abdurrahmaan_As-Sudais_192kbps",
        "Maher Al-Muaiqly" to "https://everyayah.com/data/MaherAlMuaiqly128kbps"
    )

    private var exoPlayer: ExoPlayer? = ExoPlayer.Builder(context).build()

    fun playAyah(baseUrl: String, surah: Int, ayah: Int) {
        val sStr = String.format("%03d", surah)
        val aStr = String.format("%03d", ayah)
        val url = "$baseUrl/$sStr$aStr.mp3"
        exoPlayer?.let { player ->
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
        }
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}
