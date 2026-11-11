package com.lisanulquran.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lisanulquran.app.audio.ArabicTtsManager
import com.lisanulquran.app.audio.QuranAudioPlayerManager
import com.lisanulquran.app.data.local.AyahEntity
import com.lisanulquran.app.data.local.QuranDatabase
import com.lisanulquran.app.data.local.WordEntity
import com.lisanulquran.app.ui.components.AyahCard
import com.lisanulquran.app.ui.components.LanguageChips
import com.lisanulquran.app.ui.components.ScreenTitle
import com.lisanulquran.app.ui.theme.DeepBlack

private class DailyData(
    val ayah: AyahEntity,
    val words: List<WordEntity>,
    val tafsir: String?
)

@Composable
fun DailyAyahScreen(navController: NavController) {
    val context = LocalContext.current
    val dao = remember { QuranDatabase.getDatabase(context).quranDao() }
    val player = remember { QuranAudioPlayerManager(context) }
    val tts = remember { ArabicTtsManager(context) }
    DisposableEffect(Unit) {
        onDispose {
            player.release()
            tts.release()
        }
    }
    var lang by rememberSaveable { mutableStateOf("ur") }

    val data by produceState<DailyData?>(null) {
        val day = (System.currentTimeMillis() / 86_400_000L).toInt()
        val ayah = dao.ayahAt(day % 6236)
        if (ayah != null) {
            value = DailyData(
                ayah,
                dao.wordsOf(ayah.surah, ayah.ayah),
                dao.tafsirFor(ayah.surah, ayah.ayah)?.text
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ScreenTitle("Aaj ki Ayah", "Har din nayi ayah, lafz ba lafz matlab ke saath")
        LanguageChips(lang) { lang = it }
        data?.let { d ->
            Column(Modifier.padding(top = 12.dp)) {
                AyahCard(
                    ayah = d.ayah,
                    words = d.words,
                    tafsirText = d.tafsir,
                    lang = lang,
                    showWords = true,
                    headerLabel = "${d.ayah.surah}:${d.ayah.ayah}",
                    onPlay = { player.playAyah(player.reciters[0].second, d.ayah.surah, d.ayah.ayah) },
                    onWordClick = { w -> tts.speak(w.arabic) }
                )
            }
        }
    }
}
