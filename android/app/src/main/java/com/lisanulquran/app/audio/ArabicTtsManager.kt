package com.lisanulquran.app.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

class ArabicTtsManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context, this)
    var isReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val res = tts?.isLanguageAvailable(Locale("ar", "SA"))
            if (res != null && res >= TextToSpeech.LANG_AVAILABLE) {
                tts?.language = Locale("ar", "SA")
                tts?.setSpeechRate(0.85f)
                isReady = true
            }
        }
    }

    fun speak(text: String) {
        tts?.stop() // Stop previous word to avoid overlap
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "lq_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
