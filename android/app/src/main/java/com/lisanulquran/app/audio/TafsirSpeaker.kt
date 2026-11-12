package com.lisanulquran.app.audio

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lisanulquran.app.data.tr
import java.util.Locale

/** Reads a tafsir aloud with the phone's text-to-speech (Urdu or English voice). */
object TafsirSpeaker {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var pendingStart: (() -> Unit)? = null

    /** Key of the tafsir being read right now ("surah:ayah:lang"), or null. */
    var speakingKey by mutableStateOf<String?>(null)
        private set
    var message by mutableStateOf<String?>(null)
        private set
    var messageKey by mutableStateOf<String?>(null)
        private set

    fun toggle(context: Context, key: String, text: String, lang: String) {
        if (speakingKey == key) {
            stop()
            return
        }
        message = null
        messageKey = key
        if (QuranAudioPlayerManager.isPlaying) QuranAudioPlayerManager.togglePlayPause()
        ensure(context) { start(context, key, text, lang) }
    }

    fun stop() {
        speakingKey = null
        tts?.stop()
    }

    private fun ensure(context: Context, onReady: () -> Unit) {
        if (ready && tts != null) {
            onReady()
            return
        }
        pendingStart = onReady
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    ready = true
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}
                        override fun onDone(utteranceId: String?) {
                            val id = utteranceId ?: return
                            if (id.endsWith("#last") && id.startsWith("${speakingKey}#")) speakingKey = null
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            val id = utteranceId ?: return
                            if (id.startsWith("${speakingKey}#")) speakingKey = null
                        }
                    })
                    pendingStart?.invoke()
                    pendingStart = null
                } else {
                    tts = null
                    message = tr(
                        "Phone ka text-to-speech chalu nahi ho paya.",
                        "The phone's text-to-speech could not start."
                    )
                }
            }
        }
    }

    private fun start(context: Context, key: String, text: String, lang: String) {
        val t = tts ?: return
        val locale = if (lang == "ur") Locale("ur", "PK") else Locale.US
        val res = t.setLanguage(locale)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            message = if (lang == "ur") {
                tr(
                    "Is phone mein Urdu awaaz install nahi hai. Khulne wali screen mein Urdu voice download kar lein, phir dobara dabayein.",
                    "This phone has no Urdu voice installed. Download the Urdu voice on the screen that opens, then tap again."
                )
            } else {
                tr(
                    "Is phone mein English awaaz install nahi hai.",
                    "This phone has no English voice installed."
                )
            }
            try {
                context.startActivity(
                    Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (_: Exception) {
            }
            return
        }
        t.setSpeechRate(0.9f)
        val chunks = chunk(text, minOf(TextToSpeech.getMaxSpeechInputLength() - 200, 3000))
        if (chunks.isEmpty()) return
        t.stop()
        speakingKey = key
        chunks.forEachIndexed { i, c ->
            val id = if (i == chunks.lastIndex) "$key#last" else "$key#$i"
            t.speak(c, TextToSpeech.QUEUE_ADD, null, id)
        }
    }

    private fun chunk(text: String, max: Int): List<String> {
        val out = ArrayList<String>()
        val sb = StringBuilder()
        val parts = text.split(Regex("(?<=[.!?۔؟\\n])"))
        for (p in parts) {
            if (p.isBlank()) continue
            if (sb.length + p.length > max && sb.isNotEmpty()) {
                out.add(sb.toString().trim())
                sb.clear()
            }
            if (p.length > max) {
                p.chunked(max).forEach { out.add(it.trim()) }
            } else {
                sb.append(p)
            }
        }
        if (sb.isNotBlank()) out.add(sb.toString().trim())
        return out.filter { it.isNotBlank() }
    }
}
