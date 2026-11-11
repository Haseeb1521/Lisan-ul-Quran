package com.lisanulquran.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Vocabulary : Screen("vocabulary")
    object Grammar : Screen("grammar")
    object DailyAyah : Screen("daily_ayah")
    object Tafsir : Screen("tafsir")
    object Quiz : Screen("quiz")
    object Revision : Screen("revision")
}
