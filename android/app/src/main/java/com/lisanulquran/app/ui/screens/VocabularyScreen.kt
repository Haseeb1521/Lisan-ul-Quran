package com.lisanulquran.app.ui.screens

import com.lisanulquran.app.data.AppSettings
import com.lisanulquran.app.data.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.lisanulquran.app.ui.components.SearchField
import com.lisanulquran.app.ui.theme.GoldBrush
import kotlin.math.ln
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.lisanulquran.app.data.LearnedStore
import com.lisanulquran.app.data.local.QuranDatabase
import com.lisanulquran.app.data.local.meaningOf
import com.lisanulquran.app.ui.components.AppCard
import com.lisanulquran.app.ui.components.LanguageChips
import com.lisanulquran.app.ui.components.ScreenTitle
import com.lisanulquran.app.ui.components.StarButton
import com.lisanulquran.app.ui.theme.BgBrush
import com.lisanulquran.app.ui.theme.DeepBlack
import com.lisanulquran.app.ui.theme.Emerald
import com.lisanulquran.app.ui.theme.Gold
import com.lisanulquran.app.ui.theme.Muted
import kotlinx.coroutines.delay

@Composable
fun VocabularyScreen(navController: NavController) {
    val context = LocalContext.current
    val dao = remember { QuranDatabase.getDatabase(context).quranDao() }
    val tts = remember { ArabicTtsManager(context) }
    DisposableEffect(Unit) { onDispose { tts.release() } }

    var input by rememberSaveable { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    val lang = AppSettings.meaningLang
    LaunchedEffect(input) {
        delay(300)
        query = input.trim()
    }
    val rows by remember(query) { dao.vocab(query) }.collectAsState(initial = emptyList())
    val learned = LearnedStore.words
    val maxFreq = rows.maxOfOrNull { it.freq } ?: 1

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBrush),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column {
                ScreenTitle("Vocabulary", tr("Quran ke tamam alfaz, kitni baar aaye us hisab se", "Every word of the Quran, ordered by how often it appears"))
                SearchField(input, { input = it }, tr("Arabic, matlab ya transliteration", "Arabic, meaning or transliteration"))
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LanguageChips()
                    Text("${rows.size} ${tr("alfaz", "words")}", color = Muted, fontSize = 13.sp)
                }
            }
        }
        items(rows, key = { it.arabic }) { row ->
            val frac = if (maxFreq > 1) (ln(row.freq.toFloat().coerceAtLeast(1f)) / ln(maxFreq.toFloat())).coerceIn(0.04f, 1f) else 1f
            AppCard(accent = Emerald) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).padding(start = 4.dp)) {
                        Text(row.arabic, fontSize = 34.sp, color = Gold, lineHeight = 50.sp)
                        if (row.translit.isNotBlank()) {
                            Text(
                                row.translit,
                                color = Emerald,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0x2234D399))
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                        Text(
                            meaningOf(row.en, row.ur, row.ro, lang),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (lang == "ur") 19.sp else 17.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x2234D399))
                                    .clickable { tts.speak(row.arabic) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Listen", tint = Emerald)
                            }
                            StarButton(row.arabic in learned) { LearnedStore.toggle(row.arabic) }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0x22FFFFFF))
                    ) {
                        Box(Modifier.fillMaxWidth(frac).height(4.dp).clip(RoundedCornerShape(2.dp)).background(GoldBrush))
                    }
                    Text(
                        tr("${row.freq} baar", "${row.freq}×"),
                        color = Muted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }
        }
    }
}
