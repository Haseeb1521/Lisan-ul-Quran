package com.lisanulquran.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lisanulquran.app.data.AppSettings
import com.lisanulquran.app.data.tr
import com.lisanulquran.app.data.local.AyahEntity
import com.lisanulquran.app.data.local.WordEntity
import com.lisanulquran.app.data.local.meaningOf
import com.lisanulquran.app.ui.theme.CardDark
import com.lisanulquran.app.ui.theme.CardLight
import com.lisanulquran.app.ui.theme.DeepBlack
import com.lisanulquran.app.ui.theme.Emerald
import com.lisanulquran.app.ui.theme.Gold
import com.lisanulquran.app.ui.theme.Muted

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    highlight: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val base = modifier.fillMaxWidth()
    val colors = CardDefaults.cardColors(containerColor = CardDark)
    val shape = RoundedCornerShape(18.dp)
    val border = BorderStroke(if (highlight) 2.dp else 1.dp, if (highlight) Gold else MaterialTheme.colorScheme.outline)
    if (onClick != null) {
        Card(onClick = onClick, modifier = base, colors = colors, shape = shape, border = border) {
            Column(Modifier.padding(16.dp), content = content)
        }
    } else {
        Card(modifier = base, colors = colors, shape = shape, border = border) {
            Column(Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
fun ScreenTitle(title: String, subtitle: String? = null) {
    Column(Modifier.padding(bottom = 12.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}

@Composable
fun NumberBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 38.dp, minHeight = 38.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(CardLight),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Emerald, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 10.dp))
    }
}

@Composable
fun LanguageChips() {
    val lang = AppSettings.meaningLang
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        FilterChip(selected = lang == "ro", onClick = { AppSettings.changeMeaningLang("ro") }, label = { Text("Roman") })
        FilterChip(selected = lang == "ur", onClick = { AppSettings.changeMeaningLang("ur") }, label = { Text("اردو") })
        FilterChip(selected = lang == "en", onClick = { AppSettings.changeMeaningLang("en") }, label = { Text("English") })
    }
}

@Composable
fun UiLanguageChips() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(tr("App ki zuban:", "App language:"), color = Muted, fontSize = 13.sp)
        FilterChip(selected = !AppSettings.uiEnglish, onClick = { AppSettings.changeUiEnglish(false) }, label = { Text("Hinglish") })
        FilterChip(selected = AppSettings.uiEnglish, onClick = { AppSettings.changeUiEnglish(true) }, label = { Text("English") })
    }
}

@Composable
fun StarButton(learned: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            if (learned) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = "Revision",
            tint = if (learned) Gold else Muted
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordFlow(words: List<WordEntity>, lang: String, onWordClick: (WordEntity) -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            words.forEach { w ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepBlack.copy(alpha = 0.45f))
                        .clickable { onWordClick(w) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(w.arabic, fontSize = 26.sp, color = Gold, lineHeight = 40.sp)
                    Text(
                        meaningOf(w.en, w.ur, w.ro, lang),
                        fontSize = 11.sp,
                        color = Muted,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 90.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AyahCard(
    ayah: AyahEntity,
    words: List<WordEntity>,
    tafsirText: String?,
    lang: String,
    showWords: Boolean,
    headerLabel: String = ayah.ayah.toString(),
    isCurrent: Boolean = false,
    isPlaying: Boolean = false,
    onPlay: () -> Unit,
    onWordClick: (WordEntity) -> Unit
) {
    var tafsirOpen by rememberSaveable(ayah.surah, ayah.ayah) { mutableStateOf(false) }
    AppCard(highlight = isCurrent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberBadge(headerLabel)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onPlay) {
                Icon(if (isCurrent && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play", tint = Emerald)
            }
        }
        Spacer(Modifier.height(8.dp))
        if (showWords && words.isNotEmpty()) {
            WordFlow(words, lang, onWordClick)
        } else {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    ayah.arabic,
                    fontSize = 28.sp,
                    lineHeight = 50.sp,
                    color = Gold,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        val translation = when (lang) {
            "ur" -> ayah.urdu.ifBlank { ayah.english }
            "ro" -> ayah.roman.ifBlank { ayah.english }
            else -> ayah.english.ifBlank { ayah.urdu }
        }
        Text(
            translation,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = if (lang == "ur") 19.sp else 16.sp,
            lineHeight = if (lang == "ur") 32.sp else 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (tafsirText != null) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { tafsirOpen = !tafsirOpen }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tafsir", color = Emerald, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (lang == "ur") "  Bayan-ul-Quran" else "  Ibn Kathir (Abridged, English)",
                    color = Muted,
                    fontSize = 12.sp
                )
                Icon(
                    if (tafsirOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Emerald
                )
            }
            if (tafsirOpen) {
                if (lang == "ro") {
                    Text(
                        tr("Roman mein tafsir available nahi hai, isliye English dikha rahe hain.", "A Roman tafsir isn't available, so the English one is shown."),
                        color = Gold,
                        fontSize = 12.sp
                    )
                }
                Text(
                    tafsirText,
                    color = Muted,
                    fontSize = if (lang == "ur") 17.sp else 15.sp,
                    lineHeight = if (lang == "ur") 30.sp else 23.sp
                )
            }
        }
    }
}
