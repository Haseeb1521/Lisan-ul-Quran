package com.lisanulquran.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lisanulquran.app.data.AppSettings
import com.lisanulquran.app.data.GameStore
import com.lisanulquran.app.data.LearnedStore
import com.lisanulquran.app.data.local.QuranDatabase
import com.lisanulquran.app.data.local.VocabRow
import com.lisanulquran.app.data.local.meaningOf
import com.lisanulquran.app.data.tr
import com.lisanulquran.app.ui.components.AppCard
import com.lisanulquran.app.ui.components.LanguageChips
import com.lisanulquran.app.ui.components.ScreenTitle
import com.lisanulquran.app.ui.components.StarButton
import com.lisanulquran.app.ui.navigation.Screen
import com.lisanulquran.app.ui.theme.BgBrush
import com.lisanulquran.app.ui.theme.CardBrush
import com.lisanulquran.app.ui.theme.CardLight
import com.lisanulquran.app.ui.theme.DeepBlack
import com.lisanulquran.app.ui.theme.Emerald
import com.lisanulquran.app.ui.theme.Gold
import com.lisanulquran.app.ui.theme.GoldBrush
import com.lisanulquran.app.ui.theme.HeroBrush
import com.lisanulquran.app.ui.theme.Muted
import kotlin.math.sin

private const val WORDS_PER_LEVEL = 8

@Composable
fun RevisionScreen(navController: NavController) {
    val context = LocalContext.current
    val dao = remember { QuranDatabase.getDatabase(context).quranDao() }
    val lang = AppSettings.meaningLang
    val pool by produceState<List<VocabRow>>(emptyList()) {
        value = dao.topVocab(1500).distinctBy { it.arabic }.take(WORDS_PER_LEVEL * 60)
    }
    val levels = pool.size / WORDS_PER_LEVEL
    val learned = LearnedStore.words
    var saved by remember { mutableStateOf<List<VocabRow>>(emptyList()) }
    LaunchedEffect(learned) {
        saved = if (learned.isEmpty()) emptyList() else dao.vocabByArabic(learned.toList().take(200))
    }

    val stars = GameStore.stars // read so the screen updates after a lesson
    val currentLevel = (1..maxOf(levels, 1)).firstOrNull { (stars[it] ?: 0) == 0 && GameStore.isUnlocked(it) } ?: levels

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBrush),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                ScreenTitle(
                    tr("Revision Game", "Revision Game"),
                    tr("Level poore karo, XP kamao, streak banao", "Finish levels, earn XP, build your streak")
                )
                StatsCard()
            }
        }
        item { LanguageChips() }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PracticeCard(
                    modifier = Modifier.weight(1f),
                    emoji = "⭐",
                    title = tr("Saved alfaz", "Saved words"),
                    count = learned.size
                ) { navController.navigate(Screen.Lesson.create("starred")) }
                PracticeCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🔁",
                    title = tr("Meri galtiyan", "My mistakes"),
                    count = GameStore.weak.size
                ) { navController.navigate(Screen.Lesson.create("mistakes")) }
            }
        }
        item {
            Text(
                tr("Seekhne ka raasta", "Learning path"),
                style = MaterialTheme.typography.titleLarge,
                color = Gold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (levels == 0) {
            item { Text(tr("Level tayyar ho rahe hain...", "Preparing levels..."), color = Muted) }
        }
        items((1..levels).toList(), key = { it }) { level ->
            val unlocked = GameStore.isUnlocked(level)
            LevelNode(
                level = level,
                preview = pool.getOrNull((level - 1) * WORDS_PER_LEVEL)?.arabic ?: "",
                stars = stars[level] ?: 0,
                unlocked = unlocked,
                current = level == currentLevel
            ) { navController.navigate(Screen.Lesson.create("level", level)) }
        }
        if (saved.isNotEmpty()) {
            item {
                Text(
                    tr("Mere saved alfaz", "My saved words"),
                    style = MaterialTheme.typography.titleLarge,
                    color = Gold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            items(saved, key = { "s_" + it.arabic }) { row ->
                var revealed by remember(row.arabic) { mutableStateOf(false) }
                AppCard(onClick = { revealed = !revealed }) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(row.arabic, fontSize = 30.sp, color = Gold, lineHeight = 44.sp)
                            if (revealed) {
                                if (row.translit.isNotBlank()) Text(row.translit, color = Emerald, fontSize = 13.sp)
                                Text(
                                    meaningOf(row.en, row.ur, row.ro, lang),
                                    fontSize = if (lang == "ur") 18.sp else 16.sp
                                )
                            } else {
                                Text(
                                    tr("Matlab dekhne ke liye dabayein", "Tap to reveal the meaning"),
                                    color = Muted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        StarButton(true) { LearnedStore.toggle(row.arabic) }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun StatsCard() {
    val streak = GameStore.streakNow()
    val todayXp = GameStore.xpTodayNow()
    val goal = GameStore.DAILY_GOAL
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(HeroBrush)
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Stat("🔥", "$streak", tr("din streak", "day streak"))
            Stat("⚡", "${GameStore.xp}", "XP")
            Stat("🎯", "${todayXp.coerceAtMost(goal)}/$goal", tr("aaj ka goal", "daily goal"))
        }
        Spacer(Modifier.height(14.dp))
        LinearProgressIndicator(
            progress = { (todayXp.toFloat() / goal).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = Gold,
            trackColor = Color(0x33FFFFFF)
        )
    }
}

@Composable
private fun Stat(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 26.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
        Text(label, color = Color(0xFFB7D3C6), fontSize = 12.sp)
    }
}

@Composable
private fun PracticeCard(modifier: Modifier, emoji: String, title: String, count: Int, onClick: () -> Unit) {
    val enabled = count > 0
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CardBrush)
            .border(1.dp, if (enabled) Gold.copy(alpha = 0.5f) else Color(0xFF2F4A3F), RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(14.dp)
    ) {
        Text(emoji, fontSize = 26.sp)
        Text(title, fontWeight = FontWeight.Bold, color = if (enabled) Color.White else Muted)
        Text("$count", color = if (enabled) Gold else Muted, fontSize = 13.sp)
    }
}

@Composable
private fun LevelNode(
    level: Int,
    preview: String,
    stars: Int,
    unlocked: Boolean,
    current: Boolean,
    onClick: () -> Unit
) {
    val shift = (sin(level * 0.9) * 70).toFloat().dp
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "scale"
    )
    val done = stars > 0
    val fill = when {
        done -> GoldBrush
        unlocked -> Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF0F8F6A)))
        else -> Brush.linearGradient(listOf(Color(0xFF2A3A33), Color(0xFF212D27)))
    }
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.offset(x = shift),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(78.dp)
                    .scale(if (current) pulse else 1f)
                    .clip(CircleShape)
                    .background(fill)
                    .border(4.dp, if (current) Color.White else Color(0x33FFFFFF), CircleShape)
                    .clickable(enabled = unlocked, onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (!unlocked) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Muted)
                } else {
                    Text(
                        preview,
                        fontSize = 26.sp,
                        color = DeepBlack,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
            Text(
                "Level $level",
                color = if (unlocked) Color.White else Muted,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (done) Text("⭐".repeat(stars), fontSize = 14.sp)
        }
    }
}
