package com.lisanulquran.app.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lisanulquran.app.audio.ArabicTtsManager
import com.lisanulquran.app.data.AppSettings
import com.lisanulquran.app.data.GameStore
import com.lisanulquran.app.data.LearnedStore
import com.lisanulquran.app.data.local.QuranDatabase
import com.lisanulquran.app.data.local.VocabRow
import com.lisanulquran.app.data.local.meaningOf
import com.lisanulquran.app.data.tr
import com.lisanulquran.app.ui.theme.BgBrush
import com.lisanulquran.app.ui.theme.CardDark
import com.lisanulquran.app.ui.theme.CardLight
import com.lisanulquran.app.ui.theme.DeepBlack
import com.lisanulquran.app.ui.theme.Emerald
import com.lisanulquran.app.ui.theme.ErrorRed
import com.lisanulquran.app.ui.theme.Gold
import com.lisanulquran.app.ui.theme.Muted
import kotlinx.coroutines.delay

private enum class Kind { AM, MA, LISTEN }

private sealed class Step {
    class Choice(val kind: Kind, val word: VocabRow, val options: List<VocabRow>, val answer: Int) : Step()
    class Match(val words: List<VocabRow>) : Step()
}

private fun meaning(w: VocabRow, lang: String) = meaningOf(w.en, w.ur, w.ro, lang)

private fun buildSteps(words: List<VocabRow>, pool: List<VocabRow>, lang: String, listenOk: Boolean): List<Step> {
    val steps = ArrayList<Step>()
    words.shuffled().forEachIndexed { i, w ->
        val kind = when (i % 4) {
            1 -> Kind.MA
            2 -> if (listenOk) Kind.LISTEN else Kind.AM
            else -> Kind.AM
        }
        val distractors = (pool + words)
            .filter { it.arabic != w.arabic && meaning(it, lang).isNotBlank() && meaning(it, lang) != meaning(w, lang) }
            .distinctBy { meaning(it, lang) }
            .distinctBy { it.arabic }
            .shuffled()
            .take(3)
        if (distractors.isEmpty()) return@forEachIndexed
        val options = (distractors + w).shuffled()
        steps.add(Step.Choice(kind, w, options, options.indexOf(w)))
    }
    steps.shuffle()
    val matchWords = words.distinctBy { meaning(it, lang) }.shuffled().take(4)
    if (matchWords.size >= 4) steps.add(steps.size / 2, Step.Match(matchWords))
    return steps
}

private enum class OptState { Idle, Correct, Wrong, Dim }

@Composable
private fun OptionTile(text: String, arabic: Boolean, state: OptState, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val container by animateColorAsState(
        when (state) {
            OptState.Correct -> Emerald.copy(alpha = 0.22f)
            OptState.Wrong -> ErrorRed.copy(alpha = 0.22f)
            OptState.Dim -> CardDark.copy(alpha = 0.5f)
            OptState.Idle -> CardDark
        },
        label = "container"
    )
    val border by animateColorAsState(
        when (state) {
            OptState.Correct -> Emerald
            OptState.Wrong -> ErrorRed
            else -> Color(0xFF2F4A3F)
        },
        label = "border"
    )
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(2.dp, border)
    ) {
        Text(
            text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            textAlign = TextAlign.Center,
            fontSize = if (arabic) 28.sp else 18.sp,
            color = if (arabic) Gold else Color.White,
            lineHeight = if (arabic) 40.sp else 24.sp
        )
    }
}

@Composable
private fun MatchStep(
    step: Step.Match,
    lang: String,
    speak: (String) -> Unit,
    onMistake: (VocabRow) -> Unit,
    onDone: () -> Unit
) {
    val left = remember(step) { step.words.shuffled() }
    val right = remember(step) { step.words.shuffled() }
    var selLeft by remember(step) { mutableStateOf<Int?>(null) }
    val matched = remember(step) { mutableStateListOf<String>() }
    var wrongRight by remember(step) { mutableStateOf<Int?>(null) }

    LaunchedEffect(wrongRight) {
        if (wrongRight != null) {
            delay(500)
            wrongRight = null
        }
    }

    Column {
        Text(
            tr("Jodiyan milayein", "Match the pairs"),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                left.forEachIndexed { i, w ->
                    val state = when {
                        w.arabic in matched -> OptState.Correct
                        selLeft == i -> OptState.Idle
                        else -> OptState.Idle
                    }
                    OptionTile(
                        text = w.arabic,
                        arabic = true,
                        state = if (selLeft == i) OptState.Correct else state,
                        modifier = Modifier
                    ) {
                        if (w.arabic !in matched) {
                            selLeft = i
                            speak(w.arabic)
                        }
                    }
                }
            }
            Column(Modifier.weight(1f)) {
                right.forEachIndexed { j, w ->
                    val state = when {
                        w.arabic in matched -> OptState.Correct
                        wrongRight == j -> OptState.Wrong
                        else -> OptState.Idle
                    }
                    OptionTile(text = meaning(w, lang), arabic = false, state = state) {
                        val li = selLeft
                        if (li != null && w.arabic !in matched) {
                            val lw = left[li]
                            if (lw.arabic == w.arabic) {
                                matched.add(w.arabic)
                                selLeft = null
                                if (matched.size == left.size) onDone()
                            } else {
                                wrongRight = j
                                selLeft = null
                                onMistake(lw)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonScreen(navController: NavController, kind: String, level: Int) {
    val context = LocalContext.current
    val dao = remember { QuranDatabase.getDatabase(context).quranDao() }
    val lang = AppSettings.meaningLang
    val tts = remember { ArabicTtsManager(context) }
    DisposableEffect(Unit) { onDispose { tts.release() } }
    val haptic = LocalHapticFeedback.current
    val tone = remember { runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 70) }.getOrNull() }
    DisposableEffect(Unit) { onDispose { tone?.release() } }

    var attempt by remember { mutableIntStateOf(0) }
    var loaded by remember { mutableStateOf(false) }
    var words by remember { mutableStateOf<List<VocabRow>>(emptyList()) }
    var queue by remember { mutableStateOf<List<Step>>(emptyList()) }
    var idx by remember { mutableIntStateOf(0) }
    var hearts by remember { mutableIntStateOf(5) }
    var mistakes by remember { mutableIntStateOf(0) }
    var xp by remember { mutableIntStateOf(0) }
    var initialTotal by remember { mutableIntStateOf(1) }
    var doneCount by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var feedbackOk by remember { mutableStateOf(true) }
    var finished by remember { mutableStateOf(false) }
    var failedLesson by remember { mutableStateOf(false) }
    var starsEarned by remember { mutableIntStateOf(0) }
    val failed = remember { HashSet<Step>() }
    val missed = remember { HashSet<String>() }

    LaunchedEffect(kind, level, lang, attempt) {
        loaded = false
        delay(700) // let the Arabic text-to-speech engine start so "listen" questions can be used
        val pool = dao.topVocab(1500).distinctBy { it.arabic }
        val ws = when (kind) {
            "starred" -> dao.vocabByArabic(LearnedStore.words.toList().shuffled().take(100)).shuffled().take(8)
            "mistakes" -> dao.vocabByArabic(GameStore.weak.toList().shuffled().take(100)).shuffled().take(8)
            else -> pool.drop((level - 1).coerceAtLeast(0) * 8).take(8)
        }
        val steps = if (ws.isNotEmpty()) buildSteps(ws, pool.take(300), lang, tts.isReady) else emptyList()
        words = ws
        queue = steps
        initialTotal = maxOf(steps.size, 1)
        idx = 0
        hearts = 5
        mistakes = 0
        xp = 0
        doneCount = 0
        selected = null
        feedback = null
        finished = false
        failedLesson = false
        failed.clear()
        missed.clear()
        loaded = true
    }

    val step = queue.getOrNull(idx)

    LaunchedEffect(step, loaded) {
        if (step is Step.Choice && step.kind == Kind.LISTEN) {
            delay(300)
            tts.speak(step.word.arabic)
        }
    }

    fun onMistake(w: VocabRow) {
        hearts--
        mistakes++
        missed.add(w.arabic)
        GameStore.markWeak(w.arabic)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        runCatching { tone?.startTone(ToneGenerator.TONE_PROP_NACK, 150) }
        if (hearts <= 0) failedLesson = true
    }

    fun answerChoice(i: Int, s: Step.Choice) {
        if (feedback != null) return
        selected = i
        if (i == s.answer) {
            feedbackOk = true
            feedback = tr("Shabash! 🎉", "Great job! 🎉")
            xp += if (s in failed) 5 else 10
            doneCount++
            runCatching { tone?.startTone(ToneGenerator.TONE_PROP_ACK, 120) }
        } else {
            feedbackOk = false
            val right = if (s.kind == Kind.MA) s.word.arabic else meaning(s.word, lang)
            feedback = "${tr("Sahi jawab", "Correct answer")}: $right"
            failed.add(s)
            queue = queue + s
            onMistake(s.word)
        }
    }

    fun finish() {
        val stars = when {
            mistakes == 0 -> 3
            mistakes <= 2 -> 2
            else -> 1
        }
        val bonus = if (mistakes == 0) 15 else 10
        if (kind == "mistakes") {
            words.forEach { if (it.arabic !in missed) GameStore.clearWeak(it.arabic) }
        }
        GameStore.recordLesson(if (kind == "level") level else null, stars, xp + bonus)
        xp += bonus
        starsEarned = stars
        finished = true
    }

    fun next() {
        selected = null
        feedback = null
        if (failedLesson || hearts <= 0) {
            failedLesson = true
            return
        }
        if (idx + 1 >= queue.size) finish() else idx++
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBrush)
    ) {
        // top bar
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Muted)
            }
            LinearProgressIndicator(
                progress = { (doneCount.toFloat() / initialTotal).coerceIn(0f, 1f) },
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp)),
                color = Emerald,
                trackColor = CardLight
            )
            Text(
                "❤️ $hearts",
                modifier = Modifier.padding(horizontal = 12.dp),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
        }

        when {
            !loaded -> {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(tr("Level tayyar ho raha hai...", "Getting your lesson ready..."), color = Muted)
                }
            }

            queue.isEmpty() -> {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🤔", fontSize = 56.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (kind == "starred") tr("Pehle Vocabulary mein kuch alfaz par ⭐ lagayein.", "Star a few words in Vocabulary first.")
                        else tr("Abhi koi galat lafz nahi hai. Shabash!", "No mistakes to practice right now. Nice!"),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = { navController.popBackStack() }) { Text(tr("Wapas", "Back")) }
                }
            }

            failedLesson -> {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("💔", fontSize = 72.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        tr("Dil khatam ho gaye!", "Out of hearts!"),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        tr("Koi baat nahi, dobara koshish karo.", "No worries, give it another go."),
                        color = Muted,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { attempt++ }, modifier = Modifier.fillMaxWidth()) {
                        Text(tr("Dobara koshish karo", "Try again"))
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CardLight, contentColor = Color.White)
                    ) { Text(tr("Band karo", "Quit")) }
                }
            }

            finished -> {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏆", fontSize = 80.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        tr("Level poora!", "Lesson complete!"),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold
                    )
                    Text("⭐".repeat(starsEarned) + "☆".repeat(3 - starsEarned), fontSize = 36.sp, modifier = Modifier.padding(top = 8.dp))
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatBox("⚡ XP", "+$xp")
                        StatBox(tr("🎯 Sahi", "🎯 Accuracy"), "${((initialTotal - mistakes).coerceAtLeast(0) * 100 / initialTotal)}%")
                        StatBox("🔥 Streak", "${GameStore.streakNow()}")
                    }
                    Spacer(Modifier.height(28.dp))
                    Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                        Text(tr("Aage badho", "Continue"))
                    }
                }
            }

            step != null -> {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    when (step) {
                        is Step.Choice -> {
                            Text(
                                when (step.kind) {
                                    Kind.AM -> tr("Is lafz ka matlab kya hai?", "What does this word mean?")
                                    Kind.MA -> tr("Iska Arabic lafz chuniye", "Choose the Arabic word")
                                    Kind.LISTEN -> tr("Suniye aur matlab chuniye", "Listen and choose the meaning")
                                },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(20.dp))
                            when (step.kind) {
                                Kind.AM -> Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(step.word.arabic, fontSize = 48.sp, color = Gold, lineHeight = 70.sp)
                                    IconButton(onClick = { tts.speak(step.word.arabic) }) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen", tint = Emerald)
                                    }
                                }

                                Kind.MA -> Text(
                                    meaning(step.word, lang),
                                    fontSize = 30.sp,
                                    color = Gold,
                                    lineHeight = 40.sp
                                )

                                Kind.LISTEN -> IconButton(
                                    onClick = { tts.speak(step.word.arabic) },
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(CircleShape)
                                        .background(Emerald.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Listen",
                                        tint = Emerald,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(20.dp))
                            step.options.forEachIndexed { i, opt ->
                                val chosen = selected
                                val st = when {
                                    chosen == null -> OptState.Idle
                                    i == step.answer -> OptState.Correct
                                    i == chosen -> OptState.Wrong
                                    else -> OptState.Dim
                                }
                                OptionTile(
                                    text = if (step.kind == Kind.MA) opt.arabic else meaning(opt, lang),
                                    arabic = step.kind == Kind.MA,
                                    state = st
                                ) { answerChoice(i, step) }
                            }
                        }

                        is Step.Match -> MatchStep(
                            step = step,
                            lang = lang,
                            speak = { tts.speak(it) },
                            onMistake = { w -> onMistake(w) },
                            onDone = {
                                feedbackOk = true
                                feedback = tr("Shabash! 🎉", "Great job! 🎉")
                                xp += 10
                                doneCount++
                                runCatching { tone?.startTone(ToneGenerator.TONE_PROP_ACK, 120) }
                            }
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        AnimatedVisibility(
            visible = feedback != null && !finished,
            enter = slideInVertically { it } + fadeIn(),
            exit = fadeOut()
        ) {
            val ok = feedbackOk
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(if (ok) Color(0xFF123D2C) else Color(0xFF4A1E1E))
                    .padding(16.dp)
            ) {
                Text(
                    feedback ?: "",
                    fontWeight = FontWeight.Bold,
                    color = if (ok) Emerald else ErrorRed,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { next() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (ok) Emerald else ErrorRed,
                        contentColor = DeepBlack
                    )
                ) { Text(tr("Aage badho", "Continue"), fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String) {
    Column(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardLight)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = Muted, fontSize = 12.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
    }
}
