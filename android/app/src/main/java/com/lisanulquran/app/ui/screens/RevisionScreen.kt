package com.lisanulquran.app.ui.screens

import com.lisanulquran.app.data.AppSettings
import com.lisanulquran.app.data.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lisanulquran.app.data.LearnedStore
import com.lisanulquran.app.data.local.QuranDatabase
import com.lisanulquran.app.data.local.VocabRow
import com.lisanulquran.app.data.local.meaningOf
import com.lisanulquran.app.ui.components.AppCard
import com.lisanulquran.app.ui.components.LanguageChips
import com.lisanulquran.app.ui.components.ScreenTitle
import com.lisanulquran.app.ui.components.StarButton
import com.lisanulquran.app.ui.theme.DeepBlack
import com.lisanulquran.app.ui.theme.Emerald
import com.lisanulquran.app.ui.theme.Gold
import com.lisanulquran.app.ui.theme.Muted

@Composable
fun RevisionScreen(navController: NavController) {
    val context = LocalContext.current
    val dao = remember { QuranDatabase.getDatabase(context).quranDao() }
    val learned = LearnedStore.words
    var rows by remember { mutableStateOf<List<VocabRow>>(emptyList()) }
    val lang = AppSettings.meaningLang

    LaunchedEffect(learned) {
        rows = if (learned.isEmpty()) emptyList() else dao.vocabByArabic(learned.toList())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column {
                ScreenTitle("Revision", tr("Jin alfaz par ⭐ lagaya wo yahan hain. Matlab dekhne ke liye card dabayein", "Words you starred ⭐ appear here. Tap a card to reveal its meaning"))
                LanguageChips()
            }
        }
        if (learned.isEmpty()) {
            item {
                Text(
                    tr("Abhi koi lafz save nahi hai. Vocabulary mein jaakar ⭐ dabayein.", "No words saved yet. Open Vocabulary and tap ⭐ on a word."),
                    color = Muted
                )
            }
        }
        items(rows, key = { it.arabic }) { row ->
            var revealed by remember(row.arabic) { mutableStateOf(false) }
            AppCard(onClick = { revealed = !revealed }) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text(row.arabic, fontSize = 30.sp, color = Gold, lineHeight = 44.sp)
                        if (revealed) {
                            if (row.translit.isNotBlank()) Text(row.translit, color = Emerald, fontSize = 13.sp)
                            Text(
                                meaningOf(row.en, row.ur, lang),
                                fontSize = if (lang == "ur") 18.sp else 16.sp
                            )
                        } else {
                            Text(tr("Matlab dekhne ke liye dabayein", "Tap to reveal the meaning"), color = Muted, fontSize = 12.sp)
                        }
                    }
                    StarButton(true) { LearnedStore.toggle(row.arabic) }
                }
            }
        }
    }
}
