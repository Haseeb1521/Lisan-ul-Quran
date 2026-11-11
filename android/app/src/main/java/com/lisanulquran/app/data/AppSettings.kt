package com.lisanulquran.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** App-wide settings: UI language (Hinglish/English), meaning+tafsir language (Urdu/English), reciter. */
object AppSettings {
    private var prefs: SharedPreferences? = null

    var uiEnglish by mutableStateOf(false)
        private set
    var meaningLang by mutableStateOf("ur")
        private set
    var reciterIndex by mutableIntStateOf(0)
        private set

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs = p
        uiEnglish = p.getBoolean("ui_english", false)
        meaningLang = p.getString("meaning_lang", "ur") ?: "ur"
        reciterIndex = p.getInt("reciter", 0)
    }

    fun setUiEnglish(v: Boolean) {
        uiEnglish = v
        prefs?.edit()?.putBoolean("ui_english", v)?.apply()
    }

    fun setMeaningLang(v: String) {
        meaningLang = v
        prefs?.edit()?.putString("meaning_lang", v)?.apply()
    }

    fun setReciter(i: Int) {
        reciterIndex = i
        prefs?.edit()?.putInt("reciter", i)?.apply()
    }
}

/** Picks the Hinglish or English text depending on the app language setting. */
fun tr(hinglish: String, english: String): String =
    if (AppSettings.uiEnglish) english else hinglish
