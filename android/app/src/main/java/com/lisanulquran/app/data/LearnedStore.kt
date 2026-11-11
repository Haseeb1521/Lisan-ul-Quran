package com.lisanulquran.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Words the user starred for revision. Saved in SharedPreferences. */
object LearnedStore {
    private var prefs: SharedPreferences? = null

    var words: Set<String> by mutableStateOf(emptySet())
        private set

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.getSharedPreferences("learned_words", Context.MODE_PRIVATE)
        prefs = p
        words = p.getStringSet("words", emptySet())?.toSet() ?: emptySet()
    }

    fun toggle(arabic: String) {
        words = if (arabic in words) words - arabic else words + arabic
        prefs?.edit()?.putStringSet("words", HashSet(words))?.apply()
    }
}
