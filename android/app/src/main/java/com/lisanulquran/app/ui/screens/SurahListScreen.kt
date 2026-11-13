package com.lisanulquran.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lisanulquran.app.data.local.QuranDatabase
import com.lisanulquran.app.data.tr
import com.lisanulquran.app.ui.components.AppCard
import com.lisanulquran.app.ui.components.ScreenTitle
import com.lisanulquran.app.ui.components.SearchField
import com.lisanulquran.app.ui.components.StarBadge
import com.lisanulquran.app.ui.theme.BgBrush
import com.lisanulquran.app.ui.theme.Gold
import com.lisanulquran.app.ui.theme.Muted

@Composable
fun SurahListScreen(
    navController: NavController,
    title: String,
    subtitle: String,
    routeFor: (Int) -> String
) {
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
        modifier = Modifier.fillMaxSize().background(BgBrush),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ScreenTitle(title, subtitle) }
        item {
            SearchField(query, { query = it }, tr("Surah dhoondhein (naam ya number)", "Search surah (name or number)"))
        }
        items(filtered, key = { it.id }) { ch ->
            AppCard(onClick = { navController.navigate(routeFor(ch.id)) }) {
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
    }
}
