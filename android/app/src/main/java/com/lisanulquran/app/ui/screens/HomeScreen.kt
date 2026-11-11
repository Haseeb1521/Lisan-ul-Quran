package com.lisanulquran.app.ui.screens

import com.lisanulquran.app.ui.components.UiLanguageChips
import com.lisanulquran.app.data.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lisanulquran.app.data.local.QuranDatabase
import com.lisanulquran.app.ui.components.AppCard
import com.lisanulquran.app.ui.components.NumberBadge
import com.lisanulquran.app.ui.components.ScreenTitle
import com.lisanulquran.app.ui.navigation.Screen
import com.lisanulquran.app.ui.theme.DeepBlack
import com.lisanulquran.app.ui.theme.Emerald
import com.lisanulquran.app.ui.theme.Gold
import com.lisanulquran.app.ui.theme.Muted

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
            .background(DeepBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            ScreenTitle("لسان القرآن", tr("Quran ki zuban samajhne ka safar", "A journey to understand the language of the Quran"))
        }
        item { UiLanguageChips() }
        item {
            AppCard(onClick = { navController.navigate(Screen.Grammar.route) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, contentDescription = null, tint = Emerald)
                    Column(Modifier.padding(start = 12.dp)) {
                        Text("Arabic Grammar", style = MaterialTheme.typography.titleMedium, color = Gold)
                        Text(tr("Ism, Fi'l, Harf aur buniyadi qawaid", "Ism, Fi'l, Harf and the basics"), color = Muted, fontSize = 13.sp)
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text(tr("Surah dhoondhein (naam ya number)", "Search surah (name or number)")) }
            )
        }
        items(filtered, key = { it.id }) { ch ->
            AppCard(onClick = { navController.navigate(Screen.Surah.create(ch.id)) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NumberBadge(ch.id.toString())
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(ch.nameSimple, style = MaterialTheme.typography.titleMedium)
                        Text("${ch.nameTranslated} • ${ch.versesCount} ${tr("ayat", "verses")}", color = Muted, fontSize = 12.sp)
                    }
                    Text(ch.nameArabic, fontSize = 24.sp, color = Gold)
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}
