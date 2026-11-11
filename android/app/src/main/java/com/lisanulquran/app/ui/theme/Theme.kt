package com.lisanulquran.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DeepBlack = Color(0xFF0A0A0A)
val EmeraldDark = Color(0xFF064E3B)
val Emerald = Color(0xFF10B981)
val Gold = Color(0xFFD4AF37)
val SoftWhite = Color(0xFFF3F4F6)
val CardDark = Color(0xFF111827)

private val AppColors = darkColorScheme(
    primary = Emerald,
    secondary = Gold,
    background = DeepBlack,
    surface = CardDark,
    onPrimary = DeepBlack,
    onBackground = SoftWhite,
    onSurface = SoftWhite
)

@Composable
fun LisanUlQuranTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, content = content)
}
