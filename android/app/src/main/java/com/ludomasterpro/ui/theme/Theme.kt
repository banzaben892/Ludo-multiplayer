package com.ludomasterpro.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object LudoColors {
    val Primary     = Color(0xFFFFD700)
    val PrimaryDim  = Color(0xFFE6A800)
    val Accent      = Color(0xFFE74C3C)

    val BgDeep      = Color(0xFF080810)
    val BgDark      = Color(0xFF0D0D1A)
    val BgCard      = Color(0xFF13132A)

    val TextMain    = Color(0xFFFFFFFF)
    val TextSub     = Color(0xFF888899)
    val Border      = Color(0xFF222244)
}

private val DarkColorScheme = darkColorScheme(
    primary = LudoColors.Primary,
    secondary = LudoColors.Accent,
    background = LudoColors.BgDark,
    surface = LudoColors.BgCard,
    onPrimary = LudoColors.BgDark,
    onBackground = LudoColors.TextMain,
    onSurface = LudoColors.TextMain,
    outline = LudoColors.Border
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        color = LudoColors.TextSub
    )
)

@Composable
fun LudoMasterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
