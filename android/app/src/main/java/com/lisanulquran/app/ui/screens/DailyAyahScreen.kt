package com.lisanulquran.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lisanulquran.app.audio.ArabicTtsManager
import com.lisanulquran.app.audio.QuranAudioPlayerManager
import com.lisanulquran.app.audio.TafsirSpeaker
import com.lisanulquran.app.data.AppSettings
import com.lisanulquran.app.data.local.AyahEntity
import com.lisanulquran.app.data.local.QuranDatabase
import com.lisanulquran.app.data.local.WordEntity
import com.lisanulquran.app.data.tr
import com.lisanulquran.app.ui.components.AyahCard
import com.lisanulquran.app.ui.components.LanguageChips
import com.lisanulquran.app.ui.components.ScreenTitle
import com.lisanulquran.app.ui.theme.BgBrush
import com.lisanulquran.app.ui.theme.DeepBlack

private class DailyData(
    val ayah: AyahEntity,
    val chapterName: String,
    val chapterCount: Int,
    val words: List<WordEntity>
)

@Composable
fun DailyAyahScreen(navController: NavController) {
    val context = LocalContext.current
    val dao = remember { QuranDatabase.getDatabase(context).quranDao() }
    val player = QuranAudioPlayerManager
    val tts = remember { ArabicTtsManager(context) }
    DisposableEffect(Unit) {
        onDispose {
            tts.release()
            TafsirSpeaker.stop()
        }
    }
    val lang = AppSettings.meaningLang

    val data by produceState<DailyData?>(null) {
        val day = (System.currentTimeMillis() / 86_400_000L).toInt()
        val ayah = dao.ayahAt(day % 6236)
        if (ayah != null) {
            val ch = dao.chapter(ayah.surah)
            value = DailyData(
                ayah,
                ch?.nameSimple ?: "",
                ch?.versesCount ?: ayah.ayah,
                dao.wordsOf(ayah.surah, ayah.ayah)
            )
        }
    }
    val tafsirUr by produceState<String?>(null, data) {
        val d = data
        value = if (d == null) null else dao.tafsirFor(d.ayah.surah, d.ayah.ayah, "ur")?.text
    }
    val tafsirEn by produceState<String?>(null, data) {
        val d = data
        value = if (d == null) null else dao.tafsirFor(d.ayah.surah, d.ayah.ayah, "en")?.text
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBrush)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ScreenTitle(
            tr("Aaj ki Ayah", "Today's Ayah"),
            tr("Har din nayi ayah, lafz ba lafz matlab ke saath", "A new ayah every day, with word-by-word meaning")
        )
        LanguageChips()
        data?.let { d ->
            val isCurrent = player.hasMedia && player.surahId == d.ayah.surah && player.ayahNumber == d.ayah.ayah
            Column(Modifier.padding(top = 12.dp)) {
                AyahCard(
                    ayah = d.ayah,
                    words = d.words,
                    tafsirUr = tafsirUr,
                    tafsirEn = tafsirEn,
                    lang = lang,
                    showWords = true,
                    headerLabel = "${d.chapterName} ${d.ayah.surah}:${d.ayah.ayah}",
                    isCurrent = isCurrent,
                    isPlaying = player.isPlaying,
                    onPlay = {
                        if (isCurrent) player.togglePlayPause()
                        else player.play(context, d.ayah.surah, d.chapterName, d.chapterCount, d.ayah.ayah, single = true)
                    },
                    onWordClick = { w -> tts.speak(w.arabic) }
                )
            }
        }
    }
}
