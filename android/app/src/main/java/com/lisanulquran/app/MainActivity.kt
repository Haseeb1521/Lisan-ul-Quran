package com.lisanulquran.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.lisanulquran.app.ui.navigation.Screen
import com.lisanulquran.app.ui.screens.*
import com.lisanulquran.app.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LisanUlQuranTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DeepBlack,
                    bottomBar = {
                        NavigationBar(containerColor = EmeraldDark) {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Home, "Home") },
                                label = { Text("Home") },
                                selected = currentRoute == Screen.Home.route,
                                onClick = { navController.navigate(Screen.Home.route) }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.MenuBook, "Vocab") },
                                label = { Text("Vocab") },
                                selected = currentRoute == Screen.Vocabulary.route,
                                onClick = { navController.navigate(Screen.Vocabulary.route) }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.School, "Grammar") },
                                label = { Text("Grammar") },
                                selected = currentRoute == Screen.Grammar.route,
                                onClick = { navController.navigate(Screen.Grammar.route) }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.AutoStories, "Ayah") },
                                label = { Text("Ayah") },
                                selected = currentRoute == Screen.DailyAyah.route,
                                onClick = { navController.navigate(Screen.DailyAyah.route) }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Quiz, "Quiz") },
                                label = { Text("Quiz") },
                                selected = currentRoute == Screen.Quiz.route,
                                onClick = { navController.navigate(Screen.Quiz.route) }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(navController, Screen.Home.route, Modifier.padding(innerPadding)) {
                        composable(Screen.Home.route) { HomeScreen(navController) }
                        composable(Screen.Vocabulary.route) { VocabularyScreen(navController) }
                        composable(Screen.Grammar.route) { GrammarScreen(navController) }
                        composable(Screen.DailyAyah.route) { DailyAyahScreen(navController) }
                        composable(Screen.Tafsir.route) { TafsirScreen(navController) }
                        composable(Screen.Quiz.route) { QuizScreen(navController) }
                        composable(Screen.Revision.route) { RevisionScreen(navController) }
                    }
                }
            }
        }
    }
}