package com.lisanulquran.app.ui.screens

import com.lisanulquran.app.data.AppSettings
import com.lisanulquran.app.data.tr
import androidx.compose.foundation.background
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
            .background(DeepBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ScreenTitle("Quiz", tr("Is lafz ka sahi matlab chuniye", "Choose the correct meaning of this word"))
        LanguageChips()
        Spacer(Modifier.height(16.dp))

        if (questions.isEmpty()) {
            Text(tr("Sawal tayyar ho rahe hain...", "Preparing questions..."), color = Muted)
        } else if (index >= questions.size) {
            AppCard {
                Text(
                    "Score: $score / ${questions.size}",
                    fontSize = 28.sp,
                    color = Gold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = { round++ }, modifier = Modifier.fillMaxWidth()) { Text(tr("Naya round", "New round")) }
        } else {
            val q = questions[index]
            Text("${tr("Sawal", "Question")} ${index + 1} / ${questions.size}", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            AppCard {
                Text(
                    q.word.arabic,
                    fontSize = 44.sp,
                    color = Gold,
                    lineHeight = 64.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                if (q.word.translit.isNotBlank()) {
                    Text(
                        q.word.translit,
                        color = Emerald,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            q.options.forEachIndexed { i, opt ->
                val chosen = selected
                val color = when {
                    chosen == null -> Emerald
                    i == q.answer -> Emerald
                    i == chosen -> ErrorRed
                    else -> Muted
                }
                OutlinedButton(
                    onClick = {
                        if (selected == null) {
                            selected = i
                            if (i == q.answer) score++
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
                ) {
                    Text(
                        meaningOf(opt.en, opt.ur, opt.ro, lang),
                        fontSize = if (lang == "ur") 18.sp else 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            if (selected != null) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { index++; selected = null },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (index + 1 < questions.size) tr("Agla sawal", "Next question") else tr("Nateeja dekhein", "See result")) }
            }
        }
    }
}
