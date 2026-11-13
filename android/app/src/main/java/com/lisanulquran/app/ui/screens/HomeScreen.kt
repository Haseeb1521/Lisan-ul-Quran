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
    val chapters by remember { dao.chapters() }.collectAsState(initial = emptyList())
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(chapters, query) {
        val q = query.trim()
        if (q.isEmpty()) chapters else chapters.filter {
            it.nameSimple.contains(q, ignoreCase = true) ||
                it.nameTranslated.contains(q, ignoreCase = true) ||
                it.nameArabic.contains(q) ||
                it.id.toString() == q
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBrush),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { HeroCard(navController) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Tile(
                    Modifier.weight(1f), SkyBrush, Icons.Default.MenuBook,
                    "Vocabulary", tr("Har lafz ka matlab", "Every word")
                ) { navController.navigate(Screen.Vocabulary.route) }
                Tile(
                    Modifier.weight(1f), AmberBrush, Icons.Default.AutoStories,
                    tr("Aaj ki Ayah", "Today's Ayah"), tr("Roz nayi ayah", "A new one daily")
                ) { navController.navigate(Screen.DailyAyah.route) }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Tile(
                    Modifier.weight(1f), VioletBrush, Icons.Default.Quiz,
                    "Quiz", tr("Apni jaanch karo", "Test yourself")
                ) { navController.navigate(Screen.Quiz.route) }
                Tile(
                    Modifier.weight(1f), TealBrush, Icons.Default.School,
                    "Grammar", tr("Ism, Fi'l, Harf", "Ism, Fi'l, Harf")
                ) { navController.navigate(Screen.Grammar.route) }
            }
        }
        item { UiLanguageChips() }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tr("Surahs", "Surahs"),
                    style = MaterialTheme.typography.titleLarge.copy(brush = GoldBrush),
                    modifier = Modifier.weight(1f)
                )
                Text("${filtered.size} / 114", color = Muted, fontSize = 13.sp)
            }
        }
        item {
            SearchField(query, { query = it }, tr("Surah dhoondhein (naam ya number)", "Search surah (name or number)"))
        }
        items(filtered, key = { it.id }) { ch ->
            AppCard(onClick = { navController.navigate(Screen.Surah.create(ch.id)) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StarBadge(ch.id.toString())
                    Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                        Text(ch.nameSimple, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${ch.nameTranslated} • ${ch.versesCount} ${tr("ayat", "verses")}",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                    Text(ch.nameArabic, fontSize = 26.sp, color = Gold)
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
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
    val frac = (today.toFloat() / GameStore.DAILY_GOAL).coerceIn(0f, 1f)
    val shape = RoundedCornerShape(28.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(HeroBrush)
            .border(BorderStroke(1.dp, Brush.linearGradient(listOf(Color(0x66F3D27A), Color(0x1AFFFFFF)))), shape)
            .geoPattern(Color(0x14FFFFFF), 52.dp)
            .padding(22.dp)
    ) {
        Text(
            "لسان القرآن",
            lineHeight = 64.sp,
            style = MaterialTheme.typography.headlineMedium.copy(brush = GoldBrush, fontSize = 46.sp)
        )
        Text(
            tr("Quran ki zuban samajhne ka safar", "A journey to understand the language of the Quran"),
            color = Color(0xFFCFE6DA),
            fontSize = 14.sp
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            HeroPill("🔥 $streak ${tr("din", "days")}")
            HeroPill("⚡ ${GameStore.xp} XP")
        }
        Spacer(Modifier.height(16.dp))
        Row {
            Text(tr("Aaj ka target", "Today's goal"), color = Color(0xFFCFE6DA), fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("$today / ${GameStore.DAILY_GOAL} XP", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    .fillMaxWidth(frac)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(GoldBrush)
            )
        }
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { navController.navigate(Screen.Revision.route) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DeepBlack)
        ) {
            Text(tr("Seekhna shuru karo", "Start learning"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
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
