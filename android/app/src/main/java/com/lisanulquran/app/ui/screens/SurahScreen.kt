package com.lisanulquran.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.lisanulquran.app.data.local.ChapterEntity
import com.lisanulquran.app.data.local.QuranDatabase
import com.lisanulquran.app.ui.components.AyahCard
import com.lisanulquran.app.ui.components.LanguageChips
import com.lisanulquran.app.ui.theme.DeepBlack
import com.lisanulquran.app.ui.theme.Gold
import com.lisanulquran.app.ui.theme.Muted

@Composable
fun SurahScreen(navController: NavController, surahId: Int) {
    val context = LocalContext.current
    val dao = remember { QuranDatabase.getDatabase(context).quranDao() }
    val chapter by produceState<ChapterEntity?>(null, surahId) { value = dao.chapter(surahId) }
    val ayahs by remember(surahId) { dao.ayahs(surahId) }.collectAsState(initial = emptyList())
    val words by remember(surahId) { dao.words(surahId) }.collectAsState(initial = emptyList())
    val tafsir by remember(surahId) { dao.tafsir(surahId) }.collectAsState(initial = emptyList())

    val wordsByAyah = remember(words) { words.groupBy { it.ayah } }
    val tafsirByAyah = remember(tafsir) { tafsir.associate { it.ayah to it.text } }

    var lang by rememberSaveable { mutableStateOf("ur") }
    var showWords by rememberSaveable { mutableStateOf(true) }

    val player = remember { QuranAudioPlayerManager(context) }
    val tts = remember { ArabicTtsManager(context) }
    DisposableEffect(Unit) {
        onDispose {
            player.release()
            tts.release()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Gold)
                }
                Column(Modifier.weight(1f)) {
                    Text(chapter?.nameSimple ?: "", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "${chapter?.nameTranslated ?: ""} • ${chapter?.versesCount ?: 0} ayat",
                        color = Muted,
                        fontSize = 13.sp
                    )
                }
                Text(chapter?.nameArabic ?: "", fontSize = 28.sp, color = Gold)
            }
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LanguageChips(lang) { lang = it }
                FilterChip(
                    selected = showWords,
                    onClick = { showWords = !showWords },
                    label = { Text("Lafz ba lafz") }
                )
            }
        }
        items(ayahs, key = { it.ayah }) { ayah ->
            AyahCard(
                ayah = ayah,
                words = wordsByAyah[ayah.ayah].orEmpty(),
                tafsirText = tafsirByAyah[ayah.ayah],
                lang = lang,
                showWords = showWords,
                onPlay = {
                    player.playAyah(player.reciters[0].second, ayah.surah, ayah.ayah)
                },
                onWordClick = { w -> tts.speak(w.arabic) }
            )
        }
    }
}
