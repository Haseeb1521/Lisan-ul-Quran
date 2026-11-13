package com.lisanulquran.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lisanulquran.app.audio.QuranAudioPlayerManager
import com.lisanulquran.app.data.local.QuranDatabase
import com.lisanulquran.app.data.local.QuranImporter
import com.lisanulquran.app.ui.components.MiniPlayer
import com.lisanulquran.app.ui.components.PlayerSheet
import com.lisanulquran.app.ui.navigation.Screen
import com.lisanulquran.app.ui.screens.DailyAyahScreen
import com.lisanulquran.app.ui.screens.GrammarScreen
import com.lisanulquran.app.ui.screens.HomeScreen
import com.lisanulquran.app.ui.screens.LessonScreen
import com.lisanulquran.app.ui.screens.LoadingScreen
import com.lisanulquran.app.ui.screens.QuizScreen
import com.lisanulquran.app.ui.screens.RevisionScreen
import com.lisanulquran.app.ui.screens.SurahScreen
import com.lisanulquran.app.ui.screens.SurahListScreen
import com.lisanulquran.app.data.tr
import com.lisanulquran.app.ui.screens.VocabularyScreen
import com.lisanulquran.app.ui.theme.DeepBlack
import com.lisanulquran.app.ui.theme.EmeraldDark
import com.lisanulquran.app.ui.theme.Emerald
import com.lisanulquran.app.ui.theme.Gold
import com.lisanulquran.app.ui.theme.LisanUlQuranTheme
import com.lisanulquran.app.ui.theme.Muted
import kotlinx.coroutines.delay

private class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Screen.Home.route, "Home", Icons.Default.Home),
    Tab(Screen.Vocabulary.route, "Vocab", Icons.Default.MenuBook),
    Tab(Screen.Grammar.route, "Grammar", Icons.Default.School),
    Tab(Screen.Quran.route, "Ayah", Icons.Default.AutoStories),
    Tab(Screen.Tafsir.route, "Tafsir", Icons.Default.Translate),
    Tab(Screen.Quiz.route, "Quiz", Icons.Default.Quiz),
    Tab(Screen.Revision.route, "Revision", Icons.Default.Bolt)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LisanUlQuranTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val context = LocalContext.current
    var progress by remember { mutableFloatStateOf(0f) }
    var ready by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            QuranImporter.importIfNeeded(context, QuranDatabase.getDatabase(context)) { progress = it }
            ready = true
        } catch (e: Throwable) {
            error = e.message ?: e.toString()
        }
    }

    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(ready) {
        if (ready) {
            QuranAudioPlayerManager.connect(context)
            if (Build.VERSION.SDK_INT >= 33) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (ready) MainScaffold() else LoadingScreen(progress, error)
}

@Composable
private fun MainScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var showPlayer by remember { mutableStateOf(false) }
    val inLesson = currentRoute?.startsWith("lesson") == true

    LaunchedEffect(Unit) {
        while (true) {
            QuranAudioPlayerManager.refreshPosition()
            delay(300)
        }
    }
    if (showPlayer && QuranAudioPlayerManager.hasMedia) PlayerSheet { showPlayer = false }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepBlack,
        bottomBar = {
            if (!inLesson) Column {
            MiniPlayer { showPlayer = true }
            val barShape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(barShape)
                    .background(com.lisanulquran.app.ui.theme.NavBg)
                    .border(1.dp, Color(0xFF244438), barShape)
            ) {
            NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route ||
                            (tab.route == Screen.Quran.route && currentRoute == Screen.Surah.route) ||
                            (tab.route == Screen.Tafsir.route && currentRoute == Screen.TafsirSurah.route),
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Emerald,
                            selectedTextColor = Gold,
                            indicatorColor = Color(0x3334D399),
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted
                        )
                    )
                }
            }
            }
            }
        }
    ) { innerPadding ->
        NavHost(navController, Screen.Home.route, Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(
                Screen.Surah.route,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entry ->
                SurahScreen(navController, entry.arguments?.getInt("id") ?: 1)
            }
            composable(Screen.Quran.route) {
                SurahListScreen(
                    navController,
                    tr("Quran Majeed", "The Holy Quran"),
                    tr("Sirf tilawat aur reading, ayah par tap karke sunein", "Recitation and reading only, tap an ayah to listen")
                ) { Screen.Surah.create(it) }
            }
            composable(Screen.Tafsir.route) {
                SurahListScreen(
                    navController,
                    "Tafsir",
                    tr("Lafz ba lafz matlab aur tafsir", "Word-by-word meaning and tafsir")
                ) { Screen.TafsirSurah.create(it) }
            }
            composable(
                Screen.TafsirSurah.route,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entry ->
                SurahScreen(navController, entry.arguments?.getInt("id") ?: 1, tafsirMode = true)
            }
            composable(Screen.Vocabulary.route) { VocabularyScreen(navController) }
            composable(Screen.Grammar.route) { GrammarScreen(navController) }
            composable(Screen.DailyAyah.route) { DailyAyahScreen(navController) }
            composable(Screen.Quiz.route) { QuizScreen(navController) }
            composable(Screen.Revision.route) { RevisionScreen(navController) }
            composable(
                Screen.Lesson.route,
                arguments = listOf(
                    navArgument("kind") { type = NavType.StringType },
                    navArgument("level") { type = NavType.IntType }
                )
            ) { entry ->
                LessonScreen(
                    navController,
                    entry.arguments?.getString("kind") ?: "level",
                    entry.arguments?.getInt("level") ?: 1
                )
            }
        }
    }
}
