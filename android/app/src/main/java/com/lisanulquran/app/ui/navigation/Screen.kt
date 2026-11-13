package com.lisanulquran.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Surah : Screen("surah/{id}") {
        fun create(id: Int) = "surah/$id"
    }
    object Quran : Screen("quran")
    object Tafsir : Screen("tafsir")
    object TafsirSurah : Screen("tafsir_surah/{id}") {
        fun create(id: Int) = "tafsir_surah/$id"
    }
    object Vocabulary : Screen("vocabulary")
    object Grammar : Screen("grammar")
    object DailyAyah : Screen("daily_ayah")
    object Quiz : Screen("quiz")
    object Revision : Screen("revision")
    object Lesson : Screen("lesson/{kind}/{level}") {
        fun create(kind: String, level: Int = 0) = "lesson/$kind/$level"
    }
}
