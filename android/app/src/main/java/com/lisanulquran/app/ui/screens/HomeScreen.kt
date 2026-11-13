package com.lisanulquran.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Translate
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import com.lisanulquran.app.audio.ArabicTtsManager
import com.lisanulquran.app.audio.QuranAudioPlayerManager
import com.lisanulquran.app.data.AppSettings
import com.lisanulquran.app.data.LearnedStore
import com.lisanulquran.app.data.local.VocabRow
import com.lisanulquran.app.data.local.meaningOf
import com.lisanulquran.app.ui.theme.Amber
import com.lisanulquran.app.ui.theme.Teal
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lisanulquran.app.data.GameStore
import com.lisanulquran.app.data.local.QuranDatabase
import com.lisanulquran.app.data.tr
import com.lisanulquran.app.ui.components.AppCard
import com.lisanulquran.app.ui.components.SearchField
import com.lisanulquran.app.ui.components.StarBadge
import com.lisanulquran.app.ui.components.UiLanguageChips
import com.lisanulquran.app.ui.navigation.Screen
import com.lisanulquran.app.ui.theme.AmberBrush
import com.lisanulquran.app.ui.theme.BgBrush
import com.lisanulquran.app.ui.theme.DeepBlack
import com.lisanulquran.app.ui.theme.Emerald
import com.lisanulquran.app.ui.theme.Gold
import com.lisanulquran.app.ui.theme.GoldBrush
import com.lisanulquran.app.ui.theme.HeroBrush
import com.lisanulquran.app.ui.theme.Muted
import com.lisanulquran.app.ui.theme.SkyBrush
import com.lisanulquran.app.ui.theme.TealBrush
import com.lisanulquran.app.ui.theme.VioletBrush
import com.lisanulquran.app.ui.theme.geoPattern

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val dao = remember { QuranDatabase.getDatabase(context).quranDao() }
    val lang = AppSettings.meaningLang
    val day = remember { (System.currentTimeMillis() / 86_400_000L).toInt() }

    val word by produceState<VocabRow?>(null) { value = dao.topVocab(300).distinctBy { it.arabic }.let { it.getOrNull(day % maxOf(it.size, 1)) } }
    val sample by produceState<Pair<String, String>?>(null, word) {
        val w = word ?: return@produceState
        val smp = dao.wordSample(w.arabic, 0) ?: return@produceState
        val ay = dao.ayah(smp.surah, smp.ayah) ?: return@produceState
        val ch = dao.chapter(smp.surah)
        value = ay.arabic to "${ch?.nameSimple ?: ""} ${smp.surah}:${smp.ayah}"
    }
    val daily by produceState<com.lisanulquran.app.data.local.AyahEntity?>(null) { value = dao.ayahAt(day % 6236) }
    val dailyName by produceState("", daily) { value = daily?.let { dao.chapter(it.surah)?.nameSimple } ?: "" }
    val tts = remember { ArabicTtsManager(context) }
    DisposableEffect(Unit) { onDispose { tts.release() } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBrush),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { HeroCard(navController) }

        item {
            AppCard(accent = Gold) {
                SectionLabel(tr("AAJ KA QURANI LAFZ", "DAILY QURANIC WORD"))
                word?.let { w ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(w.arabic, fontSize = 40.sp, lineHeight = 60.sp, color = Gold)
                            if (w.translit.isNotBlank()) Text(w.translit, color = Emerald, fontSize = 13.sp)
                            Text(
                                meaningOf(w.en, w.ur, w.ro, lang),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (lang == "ur") 20.sp else 18.sp
                            )
                            Text(tr("Quran mein ${w.freq} baar", "Appears ${w.freq} times"), color = Muted, fontSize = 12.sp)
                        }
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0x2234D399))
                                .clickable { tts.speak(w.arabic) },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.PlayArrow, contentDescription = "Listen", tint = Emerald) }
                    }
                    sample?.let { (ar, ref) ->
                        Spacer(Modifier.height(10.dp))
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x66000000))
                                .padding(12.dp)
                        ) {
                            Text(tr("QURANI MISAAL", "QURANIC EXAMPLE"), color = Muted, fontSize = 11.sp)
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Text(ar, color = Gold, fontSize = 20.sp, lineHeight = 36.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
                            }
                            Text(ref, color = Emerald, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlineAction(tr("Saare lafz practice karo", "Practice all words")) { navController.navigate(Screen.Revision.route) }
            }
        }

        item {
            AppCard(accent = Amber) {
                Row {
                    SectionLabel(tr("ALLAH KA PAIGHAM", "A MESSAGE FROM ALLAH"), Modifier.weight(1f))
                    Text(tr("Roz ki ayah", "Daily ayah"), color = Amber, fontSize = 11.sp)
                }
                daily?.let { d ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x66000000))
                            .padding(12.dp)
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Text(d.arabic, color = Gold, fontSize = 24.sp, lineHeight = 44.sp, maxLines = 4, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
                        }
                        Text("$dailyName ${d.surah}:${d.ayah}", color = Emerald, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    val meaning = when (lang) {
                        "ur" -> d.urdu.ifBlank { d.english }
                        "ro" -> d.roman.ifBlank { d.english }
                        else -> d.english.ifBlank { d.urdu }
                    }
                    Text(meaning, maxLines = 4, overflow = TextOverflow.Ellipsis, fontSize = if (lang == "ur") 18.sp else 15.sp, lineHeight = if (lang == "ur") 30.sp else 22.sp)
                }
                Spacer(Modifier.height(10.dp))
                OutlineAction(tr("Poora matlab aur tafsir padhein", "Read full meaning and tafsir")) { navController.navigate(Screen.DailyAyah.route) }
            }
        }

        item {
            AppCard(accent = Teal, onClick = { navController.navigate(Screen.Quran.route) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        SectionLabel(tr("ASLI QURAN TILAWAT", "AUTHENTIC QURAN RECITATIONS"))
                        Text(
                            QuranAudioPlayerManager.reciters.take(4).joinToString(" • ") { it.name },
                            color = Muted,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            tr("Ayah ba ayah ya poori surah sunein, repeat aur speed ke saath", "Listen ayah by ayah or full surahs, with repeat and speed"),
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).background(GoldBrush),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DeepBlack) }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Tile(
                    Modifier.weight(1f), TealBrush, Icons.Default.School,
                    tr("5+ Grammar sabaq", "Grammar lessons"), tr("Ism, Fi'l, Harf aur practice", "Ism, Fi'l, Harf and practice")
                ) { navController.navigate(Screen.Grammar.route) }
                Tile(
                    Modifier.weight(1f), AmberBrush, Icons.Default.Translate,
                    tr("Quran samjho", "Understand Quran"), tr("Matlab aur tafsir", "Meaning and tafsir")
                ) { navController.navigate(Screen.Tafsir.route) }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Tile(
                    Modifier.weight(1f), VioletBrush, Icons.Default.Quiz,
                    "Quiz", tr("Apni jaanch karo", "Test yourself")
                ) { navController.navigate(Screen.Quiz.route) }
                Tile(
                    Modifier.weight(1f), SkyBrush, Icons.Default.Bolt,
                    "Revision", tr("Game jaisa level path", "Game-style level path")
                ) { navController.navigate(Screen.Revision.route) }
            }
        }
        item { UiLanguageChips() }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier.padding(bottom = 8.dp),
        color = Gold,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
private fun OutlineAction(label: String, onClick: () -> Unit) {
    Text(
        label,
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x1FFFFFFF))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    )
}

@Composable
private fun Tile(
    modifier: Modifier,
    brush: Brush,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier
            .clip(shape)
            .background(brush)
            .border(1.dp, Color(0x22FFFFFF), shape)
            .clickable(onClick = onClick)
            .geoPattern(Color(0x0FFFFFFF), 36.dp)
            .padding(16.dp)
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x26FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }
        Spacer(Modifier.height(14.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text(subtitle, color = Color(0xCCFFFFFF), fontSize = 12.sp)
    }
}

@Composable
private fun HeroCard(navController: NavController) {
    val streak = GameStore.streakNow()
    val today = GameStore.xpTodayNow()
    val journey = (GameStore.stars.values.sum() / 180f).coerceIn(0f, 1f)
    val shape = RoundedCornerShape(28.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(HeroBrush)
            .border(BorderStroke(1.dp, Brush.linearGradient(listOf(Color(0x66F3D27A), Color(0x1AFFFFFF)))), shape)
            .geoPattern(Color(0x14FFFFFF), 52.dp)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "LISAN-UL-QURAN • لسان القرآن",
                color = Teal,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1f)
            )
            HeroPill("🔥 $streak ${tr("din", "days")}")
        }
        Spacer(Modifier.height(12.dp))
        Text(
            tr("Assalamu Alaikum, Talib-e-ilm!", "Assalamu Alaikum, Learner!"),
            style = MaterialTheme.typography.headlineMedium.copy(brush = GoldBrush, fontSize = 26.sp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            tr("Quran ko seedha Arabic mein samajhne ka safar shuru karo, bina tarjume ki rukawat ke.", "Begin your journey to understand the Holy Quran directly in Arabic, without translation barriers."),
            color = Color(0xFFCFE6DA),
            fontSize = 13.sp
        )
        Spacer(Modifier.height(16.dp))
        Row {
            Text(tr("Qurani Fluency Safar", "Quranic Fluency Journey"), color = Color(0xFFCFE6DA), fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("${(journey * 100).toInt()}%", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0x33000000))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(journey.coerceAtLeast(0.02f))
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(GoldBrush)
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat(Modifier.weight(1f), "${LearnedStore.words.size}", tr("Saved lafz", "Saved words"))
            Stat(Modifier.weight(1f), "${GameStore.stars.size}", tr("Levels poore", "Levels done"))
            Stat(Modifier.weight(1f), "$today/${GameStore.DAILY_GOAL}", tr("Aaj ka XP", "Today's XP"))
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate(Screen.Revision.route) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = DeepBlack)
        ) {
            Text(tr("Qurani Vocabulary seekhna jaari rakho  →", "Continue Learning Quranic Vocabulary  →"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun Stat(modifier: Modifier, value: String, label: String) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x40000000))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text(label, color = Color(0xFFCFE6DA), fontSize = 11.sp)
    }
}

@Composable
private fun HeroPill(text: String) {
    Text(
        text,
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0x26FFFFFF))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}
