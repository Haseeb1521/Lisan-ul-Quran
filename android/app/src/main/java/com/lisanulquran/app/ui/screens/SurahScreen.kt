package com.lisanulquran.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lisanulquran.app.audio.ArabicTtsManager
import com.lisanulquran.app.audio.QuranAudioPlayerManager
import com.lisanulquran.app.audio.TafsirSpeaker
import com.lisanulquran.app.data.AppSettings
import com.lisanulquran.app.data.local.ChapterEntity
import com.lisanulquran.app.data.local.QuranDatabase
import com.lisanulquran.app.data.tr
import com.lisanulquran.app.ui.components.AyahCard
import com.lisanulquran.app.ui.components.LanguageChips
import com.lisanulquran.app.ui.theme.BgBrush
import com.lisanulquran.app.ui.theme.DeepBlack
import com.lisanulquran.app.ui.theme.Gold
import com.lisanulquran.app.ui.theme.Muted

@Composable
fun SurahScreen(navController: NavController, surahId: Int) {
    val context = LocalContext.current
    val dao = remember { QuranDatabase.getDatabase(context).quranDao() }
    val lang = AppSettings.meaningLang
    val chapter by produceState<ChapterEntity?>(null, surahId) { value = dao.chapter(surahId) }
    val ayahs by remember(surahId) { dao.ayahs(surahId) }.collectAsState(initial = emptyList())
    val words by remember(surahId) { dao.words(surahId) }.collectAsState(initial = emptyList())
    val tafsirUrList by remember(surahId) { dao.tafsir(surahId, "ur") }.collectAsState(initial = emptyList())
    val tafsirEnList by remember(surahId) { dao.tafsir(surahId, "en") }.collectAsState(initial = emptyList())

    val wordsByAyah = remember(words) { words.groupBy { it.ayah } }
    val tafsirUrByAyah = remember(tafsirUrList) { tafsirUrList.associate { it.ayah to it.text } }
    val tafsirEnByAyah = remember(tafsirEnList) { tafsirEnList.associate { it.ayah to it.text } }

    var showWords by rememberSaveable { mutableStateOf(true) }

    val player = QuranAudioPlayerManager
    val tts = remember { ArabicTtsManager(context) }
    DisposableEffect(Unit) {
        onDispose {
            tts.release()
            TafsirSpeaker.stop()
        }
    }

    val thisSurahLoaded = player.hasMedia && player.surahId == surahId
    val listState = rememberLazyListState()
    val name = chapter?.nameSimple ?: ""
    val count = chapter?.versesCount ?: 0

    LaunchedEffect(player.ayahNumber, thisSurahLoaded) {
        if (thisSurahLoaded && player.ayahNumber > 0) {
            listState.animateScrollToItem((player.ayahNumber - 1 + 3).coerceAtLeast(0))
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(BgBrush),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Gold)
                }
                Column(Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "${chapter?.nameTranslated ?: ""} • $count ${tr("ayat", "verses")}",
                        color = Muted,
                        fontSize = 13.sp
                    )
                }
                Text(chapter?.nameArabic ?: "", fontSize = 28.sp, color = Gold)
            }
        }
        item {
            Button(
                onClick = {
                    if (thisSurahLoaded) player.togglePlayPause()
                    else player.play(context, surahId, name, count, 1)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Gold, contentColor = com.lisanulquran.app.ui.theme.DeepBlack)
            ) {
                val playing = thisSurahLoaded && player.isPlaying
                Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                Text(
                    if (playing) tr("Ruk jayein", "Pause")
                    else tr("Poori surah sunein", "Play full surah"),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LanguageChips()
                FilterChip(
                    selected = showWords,
                    onClick = { showWords = !showWords },
                    label = { Text(tr("Lafz ba lafz", "Word by word")) }
                )
            }
        }
        items(ayahs, key = { it.ayah }) { ayah ->
            val isCurrent = thisSurahLoaded && player.ayahNumber == ayah.ayah
            AyahCard(
                ayah = ayah,
                words = wordsByAyah[ayah.ayah].orEmpty(),
                tafsirUr = tafsirUrByAyah[ayah.ayah],
                tafsirEn = tafsirEnByAyah[ayah.ayah],
                lang = lang,
                showWords = showWords,
                isCurrent = isCurrent,
                isPlaying = player.isPlaying,
                onPlay = {
                    if (isCurrent) player.togglePlayPause()
                    else player.play(context, surahId, name, count, ayah.ayah)
                },
                onWordClick = { w -> tts.speak(w.arabic) }
            )
        }
    }
}
