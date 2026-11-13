package com.lisanulquran.app.ui.screens

import com.lisanulquran.app.data.AppSettings
import com.lisanulquran.app.data.tr
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.lisanulquran.app.ui.theme.GoldBrush
import com.lisanulquran.app.ui.theme.HeroBrush
import com.lisanulquran.app.ui.theme.geoPattern
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lisanulquran.app.data.local.QuranDatabase
import com.lisanulquran.app.data.local.VocabRow
import com.lisanulquran.app.data.local.meaningOf
import com.lisanulquran.app.ui.components.AppCard
import com.lisanulquran.app.ui.components.LanguageChips
import com.lisanulquran.app.ui.components.ScreenTitle
import com.lisanulquran.app.ui.theme.BgBrush
import com.lisanulquran.app.ui.theme.DeepBlack
import com.lisanulquran.app.ui.theme.Emerald
import com.lisanulquran.app.ui.theme.ErrorRed
import com.lisanulquran.app.ui.theme.Gold
import com.lisanulquran.app.ui.theme.Muted

private class QuizQuestion(val word: VocabRow, val options: List<VocabRow>, val answer: Int)

private const val ROUND_SIZE = 10

@Composable
fun QuizScreen(navController: NavController) {
    val context = LocalContext.current
    val dao = remember { QuranDatabase.getDatabase(context).quranDao() }

    val lang = AppSettings.meaningLang
    var round by remember { mutableIntStateOf(0) }
    var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(round, lang) {
        val pool = dao.randomVocab(120)
            .filter { meaningOf(it.en, it.ur, it.ro, lang).isNotBlank() }
            .distinctBy { meaningOf(it.en, it.ur, it.ro, lang) }
        val list = ArrayList<QuizQuestion>()
        if (pool.size >= 4) {
            for (i in 0 until minOf(ROUND_SIZE, pool.size)) {
                val correct = pool[i]
                val wrong = pool.filter { it !== correct }.shuffled().take(3)
                val options = (wrong + correct).shuffled()
                list.add(QuizQuestion(correct, options, options.indexOf(correct)))
            }
        }
        questions = list
        index = 0
        score = 0
        selected = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBrush)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ScreenTitle("Quiz", tr("Is lafz ka sahi matlab chuniye", "Choose the correct meaning of this word"))
        LanguageChips()
        Spacer(Modifier.height(16.dp))

        if (questions.isEmpty()) {
            Text(tr("Sawal tayyar ho rahe hain...", "Preparing questions..."), color = Muted)
        } else if (index >= questions.size) {
            val ratio = score.toFloat() / questions.size
            val msg = when {
                ratio >= 0.9f -> tr("Mashallah! Kamaal kar diya", "Mashallah! Outstanding")
                ratio >= 0.6f -> tr("Bahut accha, aur practice karo", "Great job, keep practising")
                else -> tr("Koi baat nahi, dobara koshish karo", "No worries, try again")
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(HeroBrush)
                    .geoPattern(Color(0x14FFFFFF), 52.dp)
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(if (ratio >= 0.6f) "🏆" else "💪", fontSize = 64.sp)
                Spacer(Modifier.height(8.dp))
                Text("$score / ${questions.size}", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = Gold)
                Text(msg, color = Color(0xFFCFE6DA), textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { round++ },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DeepBlack)
            ) { Text(tr("Naya round", "New round"), fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        } else {
            val q = questions[index]
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0x22FFFFFF))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth((index + (if (selected != null) 1 else 0)).toFloat() / questions.size)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(GoldBrush)
                    )
                }
                Text(
                    "${index + 1}/${questions.size}",
                    color = Muted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
                Text(
                    "⭐ $score",
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(HeroBrush)
                    .border(1.dp, Color(0x33F3D27A), RoundedCornerShape(26.dp))
                    .geoPattern(Color(0x12FFFFFF), 52.dp)
                    .padding(vertical = 28.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    q.word.arabic,
                    fontSize = 52.sp,
                    color = Gold,
                    lineHeight = 74.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                if (q.word.translit.isNotBlank()) {
                    Text(
                        q.word.translit,
                        color = Color(0xFFCFE6DA),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x26FFFFFF))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            q.options.forEachIndexed { i, opt ->
                val chosen = selected
                val isRight = chosen != null && i == q.answer
                val isWrong = chosen != null && i == chosen && i != q.answer
                val bg by animateColorAsState(
                    when {
                        isRight -> Color(0xFF14573F)
                        isWrong -> Color(0xFF5A1F22)
                        else -> Color(0xFF12241C)
                    },
                    label = "opt"
                )
                val edge = when {
                    isRight -> Emerald
                    isWrong -> ErrorRed
                    else -> Color(0xFF284B3D)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(bg)
                        .border(1.5.dp, edge, RoundedCornerShape(18.dp))
                        .clickable {
                            if (selected == null) {
                                selected = i
                                if (i == q.answer) score++
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0x26FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(("ABCD"[i % 4]).toString(), color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text(
                        meaningOf(opt.en, opt.ur, opt.ro, lang),
                        fontSize = if (lang == "ur") 19.sp else 16.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                    )
                    if (isRight) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald)
                    if (isWrong) Icon(Icons.Default.Cancel, contentDescription = null, tint = ErrorRed)
                }
            }
            if (selected != null) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { index++; selected = null },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DeepBlack)
                ) {
                    Text(
                        if (index + 1 < questions.size) tr("Agla sawal", "Next question") else tr("Nateeja dekhein", "See result"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
