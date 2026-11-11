package com.lisanulquran.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lisanulquran.app.audio.ArabicTtsManager
import com.lisanulquran.app.audio.QuranAudioPlayerManager
import com.lisanulquran.app.ui.navigation.Screen
import com.lisanulquran.app.ui.theme.CardDark
import com.lisanulquran.app.ui.theme.DeepBlack
import com.lisanulquran.app.ui.theme.Emerald
import com.lisanulquran.app.ui.theme.Gold

private data class Word(val arabic: String, val meaning: String)
private data class Lesson(val title: String, val body: String)
private data class Question(val arabic: String, val options: List<String>, val answer: Int)

private val words = listOf(
    Word("كِتَاب", "Kitab - Book"),
    Word("قَلَم", "Qalam - Pen"),
    Word("مَاء", "Maa - Water"),
    Word("شَمْس", "Shams - Sun"),
    Word("قَمَر", "Qamar - Moon"),
    Word("بَيْت", "Bayt - House"),
    Word("عِلْم", "Ilm - Knowledge"),
    Word("رَحْمَة", "Rahmah - Mercy")
)

private val lessons = listOf(
    Lesson("Ism (Noun)", "Ism woh lafz hai jo kisi insan, jagah ya cheez ka naam batata hai. Jaise: كِتَاب (kitab)."),
    Lesson("Fi'l (Verb)", "Fi'l woh lafz hai jo kaam aur waqt batata hai. Jaise: كَتَبَ (usne likha)."),
    Lesson("Harf (Particle)", "Harf woh lafz hai jiska matlab doosre lafz ke saath milkar poora hota hai. Jaise: فِي (mein), مِنْ (se).")
)

private val questions = listOf(
    Question("كِتَاب", listOf("Book", "Pen", "Water"), 0),
    Question("مَاء", listOf("Sun", "Water", "House"), 1),
    Question("قَمَر", listOf("Moon", "Knowledge", "Mercy"), 0),
    Question("عِلْم", listOf("Pen", "House", "Knowledge"), 2)
)

@Composable
private fun ScreenScaffold(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = Gold)
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    ScreenScaffold("Lisan-ul-Quran") {
        Text(
            "Quran ki zuban seekhein: alfaz, grammar aur roz ki ayah.",
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))
        listOf(
            "Vocabulary" to Screen.Vocabulary.route,
            "Grammar" to Screen.Grammar.route,
            "Daily Ayah" to Screen.DailyAyah.route,
            "Tafsir" to Screen.Tafsir.route,
            "Quiz" to Screen.Quiz.route,
            "Revision" to Screen.Revision.route
        ).forEach { (label, route) ->
            Button(
                onClick = { navController.navigate(route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) { Text(label) }
        }
    }
}

@Composable
fun VocabularyScreen(navController: NavController) {
    val context = LocalContext.current
    val tts = remember { ArabicTtsManager(context) }
    DisposableEffect(Unit) { onDispose { tts.stop() } }

    ScreenScaffold("Vocabulary") {
        words.forEach { word ->
            InfoCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(word.arabic, fontSize = 28.sp, color = Gold)
                        Text(word.meaning, color = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { tts.speak(word.arabic) }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Listen", tint = Emerald)
                    }
                }
            }
        }
    }
}

@Composable
fun GrammarScreen(navController: NavController) {
    ScreenScaffold("Grammar") {
        lessons.forEach { lesson ->
            InfoCard {
                Text(lesson.title, style = MaterialTheme.typography.titleMedium, color = Gold)
                Spacer(Modifier.height(6.dp))
                Text(lesson.body, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun DailyAyahScreen(navController: NavController) {
    val context = LocalContext.current
    val player = remember { QuranAudioPlayerManager(context) }

    ScreenScaffold("Daily Ayah") {
        InfoCard {
            Text("Surah Al-Fatihah, Ayah 1", color = Emerald)
            Spacer(Modifier.height(12.dp))
            Text(
                "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                fontSize = 30.sp,
                color = Gold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Allah ke naam se jo bada meherban, nihayat raham karne wala hai.",
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { player.playAyah(player.reciters[0].second, 1, 1) }) {
            Text("Play (${player.reciters[0].first})")
        }
    }
}

@Composable
fun TafsirScreen(navController: NavController) {
    ScreenScaffold("Tafsir") {
        InfoCard {
            Text("Surah Al-Fatihah", style = MaterialTheme.typography.titleMedium, color = Gold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Yeh Quran ki pehli surah hai aur har namaz mein padhi jati hai. " +
                    "Isme Allah ki tareef, uski rahmat aur seedhe raaste ki dua hai.",
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun QuizScreen(navController: NavController) {
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    ScreenScaffold("Quiz") {
        if (finished) {
            InfoCard {
                Text("Score: $score / ${questions.size}", fontSize = 24.sp, color = Gold)
            }
            Button(onClick = { index = 0; score = 0; finished = false }) { Text("Dobara khelein") }
        } else {
            val q = questions[index]
            InfoCard {
                Text("Is lafz ka matlab?", color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                Text(q.arabic, fontSize = 32.sp, color = Gold)
            }
            q.options.forEachIndexed { i, option ->
                OutlinedButton(
                    onClick = {
                        if (i == q.answer) score++
                        if (index + 1 < questions.size) index++ else finished = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) { Text(option) }
            }
        }
    }
}

@Composable
fun RevisionScreen(navController: NavController) {
    val learned = remember { mutableStateOf(setOf<Int>()) }

    ScreenScaffold("Revision") {
        Text(
            "Yaad ho chuke alfaz par tick karein: ${learned.value.size} / ${words.size}",
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        words.forEachIndexed { i, word ->
            InfoCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${word.arabic}  -  ${word.meaning}", color = MaterialTheme.colorScheme.onSurface)
                    Checkbox(
                        checked = i in learned.value,
                        onCheckedChange = { checked ->
                            learned.value = if (checked) learned.value + i else learned.value - i
                        }
                    )
                }
            }
        }
    }
}
