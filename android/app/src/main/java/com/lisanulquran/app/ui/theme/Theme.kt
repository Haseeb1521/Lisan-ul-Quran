package com.lisanulquran.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
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
val Sky = Color(0xFF38BDF8)
val Violet = Color(0xFFA78BFA)
val Amber = Color(0xFFFBBF24)
val Teal = Color(0xFF2DD4BF)
val NavBg = Color(0xFF0B1A14)

val SkyBrush = Brush.linearGradient(listOf(Color(0xFF0E4A6B), Color(0xFF0B2F45)))
val VioletBrush = Brush.linearGradient(listOf(Color(0xFF4B3A8C), Color(0xFF2A2158)))
val AmberBrush = Brush.linearGradient(listOf(Color(0xFF8A5A10), Color(0xFF4A2F08)))
val TealBrush = Brush.linearGradient(listOf(Color(0xFF0F6B5C), Color(0xFF0A3D35)))

/** Soft 8-point-star tiling, a classic Islamic geometric motif. */
fun Modifier.geoPattern(color: Color = Color(0x12FFFFFF), cell: Dp = 46.dp): Modifier = drawBehind {
    val c = cell.toPx()
    val cols = (size.width / c).toInt() + 1
    val rows = (size.height / c).toInt() + 1
    val stroke = Stroke(width = 1.dp.toPx())
    for (i in 0..cols) for (j in 0..rows) {
        val cx = i * c
        val cy = j * c
        val r = c * 0.42f
        val p = Path()
        for (k in 0 until 16) {
            val a = (k * Math.PI / 8).toFloat()
            val rad = if (k % 2 == 0) r else r * 0.6f
            val x = cx + rad * cos(a)
            val y = cy + rad * sin(a)
            if (k == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        p.close()
        drawPath(p, color, style = stroke)
    }
}

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
