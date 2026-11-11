package com.lisanulquran.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.lisanulquran.app.audio.QuranAudioPlayerManager
import com.lisanulquran.app.data.AppSettings
import com.lisanulquran.app.data.local.AyahEntity
import com.lisanulquran.app.data.local.QuranDatabase
import com.lisanulquran.app.data.tr
import com.lisanulquran.app.ui.theme.CardLight
import com.lisanulquran.app.ui.theme.DeepBlack
import com.lisanulquran.app.ui.theme.Emerald
import com.lisanulquran.app.ui.theme.ErrorRed
import com.lisanulquran.app.ui.theme.Gold
import com.lisanulquran.app.ui.theme.Muted
import com.lisanulquran.app.ui.theme.Surface1

private fun fmt(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(total / 60, total % 60)
}

@Composable
fun MiniPlayer(onOpen: () -> Unit) {
    val p = QuranAudioPlayerManager
    if (!p.hasMedia) return
    val reciter = p.reciters[AppSettings.reciterIndex.coerceIn(0, p.reciters.lastIndex)]
    Column(
        Modifier
            .fillMaxWidth()
            .background(CardLight)
    ) {
        LinearProgressIndicator(
            progress = {
                if (p.durationMs > 0) (p.positionMs.toFloat() / p.durationMs).coerceIn(0f, 1f) else 0f
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            color = Emerald,
            trackColor = DeepBlack
        )
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { onOpen() }
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${p.surahName} • ${tr("Ayah", "Verse")} ${p.ayahNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Gold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    p.error ?: reciter.name,
                    color = if (p.error != null) ErrorRed else Muted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = { p.previous() }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Emerald)
            }
            IconButton(onClick = { p.togglePlayPause() }) {
                Icon(
                    if (p.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Emerald
                )
            }
            IconButton(onClick = { p.next() }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Emerald)
            }
            IconButton(onClick = { p.stop() }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Muted)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val p = QuranAudioPlayerManager
    val dao = remember { QuranDatabase.getDatabase(context).quranDao() }
    val ayah by produceState<AyahEntity?>(null, p.surahId, p.ayahNumber) {
        value = if (p.surahId > 0) dao.ayah(p.surahId, p.ayahNumber) else null
    }
    var dragging by remember { mutableStateOf<Float?>(null) }
    val duration = p.durationMs.coerceAtLeast(1L).toFloat()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Surface1
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                p.surahName,
                style = MaterialTheme.typography.titleLarge,
                color = Gold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                buildString {
                    append(tr("Ayah", "Verse")).append(' ').append(p.ayahNumber)
                    if (p.itemCount > 1) append(" / ").append(p.itemCount)
                },
                color = Muted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            ayah?.let { a ->
                Text(
                    a.arabic,
                    fontSize = 26.sp,
                    lineHeight = 46.sp,
                    color = Gold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                val tl = if (AppSettings.meaningLang == "ur") a.urdu.ifBlank { a.english } else a.english.ifBlank { a.urdu }
                Text(
                    tl,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = if (AppSettings.meaningLang == "ur") 18.sp else 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(16.dp))
            Slider(
                value = (dragging ?: p.positionMs.toFloat()).coerceIn(0f, duration),
                onValueChange = { dragging = it },
                onValueChangeFinished = {
                    dragging?.let { p.seekTo(it.toLong()) }
                    dragging = null
                },
                valueRange = 0f..duration,
                colors = SliderDefaults.colors(thumbColor = Emerald, activeTrackColor = Emerald)
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(fmt((dragging ?: p.positionMs.toFloat()).toLong()), color = Muted, fontSize = 12.sp)
                Text(fmt(p.durationMs), color = Muted, fontSize = 12.sp)
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { p.cycleRepeat() }) {
                    Icon(
                        if (p.repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (p.repeatMode == Player.REPEAT_MODE_OFF) Muted else Gold
                    )
                }
                IconButton(onClick = { p.previous() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Emerald, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { p.togglePlayPause() }, modifier = Modifier.size(64.dp)) {
                    Icon(
                        if (p.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Emerald,
                        modifier = Modifier.size(52.dp)
                    )
                }
                IconButton(onClick = { p.next() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Emerald, modifier = Modifier.size(36.dp))
                }
                OutlinedButton(onClick = {
                    val speeds = listOf(0.75f, 1f, 1.25f, 1.5f)
                    val idx = speeds.indexOfFirst { it == p.speed }
                    p.setSpeed(speeds[(idx + 1) % speeds.size])
                }) { Text("${p.speed}x") }
            }
            if (p.isBuffering) {
                Text(
                    tr("Load ho raha hai...", "Loading..."),
                    color = Muted,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            p.error?.let {
                Text(it, color = ErrorRed, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(16.dp))
            Text(tr("Qaari (Reciter)", "Reciter"), style = MaterialTheme.typography.titleMedium, color = Gold)
            p.reciters.forEachIndexed { i, r ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { p.changeReciter(context, i) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = AppSettings.reciterIndex == i, onClick = { p.changeReciter(context, i) })
                    Text(r.name, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
