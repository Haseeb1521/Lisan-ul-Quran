package com.lisanulquran.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DeepBlack = Color(0xFF08100D)
val Surface1 = Color(0xFF0F1B16)
val CardDark = Color(0xFF14241D)
val CardLight = Color(0xFF1B3128)
val EmeraldDark = Color(0xFF0B3B2E)
val Emerald = Color(0xFF34D399)
val Gold = Color(0xFFE6BE5A)
val SoftWhite = Color(0xFFEAF3EE)
val Muted = Color(0xFF9DB5A9)
val ErrorRed = Color(0xFFF87171)

val BgBrush = Brush.verticalGradient(listOf(Color(0xFF0C2119), Color(0xFF08100D), Color(0xFF060B09)))
val CardBrush = Brush.linearGradient(listOf(Color(0xFF173529), Color(0xFF112119)))
val GoldBrush = Brush.linearGradient(listOf(Color(0xFFF3D27A), Color(0xFFD4A63A)))
val HeroBrush = Brush.linearGradient(listOf(Color(0xFF0F5A43), Color(0xFF0B3B2E), Color(0xFF123F33)))

private val AppColors = darkColorScheme(
    primary = Emerald,
    onPrimary = DeepBlack,
    secondary = Gold,
    onSecondary = DeepBlack,
    background = DeepBlack,
    onBackground = SoftWhite,
    surface = CardDark,
    onSurface = SoftWhite,
    surfaceVariant = CardLight,
    onSurfaceVariant = Muted,
    outline = Color(0xFF2F4A3F),
    error = ErrorRed
)

private val AppTypography = Typography(
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Gold),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    labelMedium = TextStyle(fontSize = 12.sp)
)

@Composable
fun LisanUlQuranTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, typography = AppTypography, content = content)
}
