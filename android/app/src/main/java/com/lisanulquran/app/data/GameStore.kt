package com.lisanulquran.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.TimeZone

/** XP, streak, level stars and weak words for the revision game. Saved in SharedPreferences. */
object GameStore {
    const val DAILY_GOAL = 30

    private var prefs: SharedPreferences? = null

    var xp by mutableIntStateOf(0)
        private set
    private var streak by mutableIntStateOf(0)
    private var lastDay by mutableLongStateOf(-1L)
    private var xpToday by mutableIntStateOf(0)
    private var xpDay by mutableLongStateOf(-1L)
    var stars by mutableStateOf<Map<Int, Int>>(emptyMap())
        private set
    var weak by mutableStateOf<Set<String>>(emptySet())
        private set

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.getSharedPreferences("game_store", Context.MODE_PRIVATE)
        prefs = p
        xp = p.getInt("xp", 0)
        streak = p.getInt("streak", 0)
        lastDay = p.getLong("last_day", -1L)
        xpToday = p.getInt("xp_today", 0)
        xpDay = p.getLong("xp_day", -1L)
        weak = p.getStringSet("weak", emptySet())?.toSet() ?: emptySet()
        stars = (p.getString("stars", "") ?: "")
            .split(",")
            .mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) {
                    val l = parts[0].toIntOrNull()
                    val s = parts[1].toIntOrNull()
                    if (l != null && s != null) l to s else null
                } else null
            }
            .toMap()
    }

    private fun today(): Long {
        val now = System.currentTimeMillis()
        return (now + TimeZone.getDefault().getOffset(now)) / 86_400_000L
    }

    fun streakNow(): Int {
        val t = today()
        return if (lastDay == t || lastDay == t - 1) streak else 0
    }

    fun doneToday(): Boolean = lastDay == today()

    fun xpTodayNow(): Int = if (xpDay == today()) xpToday else 0

    fun starsOf(level: Int): Int = stars[level] ?: 0

    fun isUnlocked(level: Int): Boolean = level <= 1 || starsOf(level - 1) > 0

    /** Called when a lesson is finished. [level] is null for practice lessons. */
    fun recordLesson(level: Int?, starsEarned: Int, xpEarned: Int) {
        val t = today()
        if (lastDay != t) {
            streak = if (lastDay == t - 1) streak + 1 else 1
            lastDay = t
        }
        if (xpDay != t) {
            xpDay = t
            xpToday = 0
        }
        xpToday += xpEarned
        xp += xpEarned
        if (level != null && starsEarned > starsOf(level)) {
            stars = stars + (level to starsEarned)
        }
        prefs?.edit()
            ?.putInt("xp", xp)
            ?.putInt("streak", streak)
            ?.putLong("last_day", lastDay)
            ?.putInt("xp_today", xpToday)
            ?.putLong("xp_day", xpDay)
            ?.putString("stars", stars.entries.joinToString(",") { "${it.key}:${it.value}" })
            ?.apply()
    }

    fun markWeak(arabic: String) {
        if (arabic in weak) return
        weak = weak + arabic
        prefs?.edit()?.putStringSet("weak", HashSet(weak))?.apply()
    }

    fun clearWeak(arabic: String) {
        if (arabic !in weak) return
        weak = weak - arabic
        prefs?.edit()?.putStringSet("weak", HashSet(weak))?.apply()
    }
}
