package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class SiloColors(
    val background: Color,
    val surface: Color,
    val onBackground: Color,
    val onSurface: Color,
    val primary: Color,
    val onPrimary: Color,
    val accent: Color,
    val inactive: Color
)

val CharcoalZenColors = SiloColors(
    background = Color(0xFF0D0D0D),
    surface = Color(0xFF161616),
    onBackground = Color(0xFFF2F2EB),
    onSurface = Color(0xFF8DB5B2),
    primary = Color(0xFFF2F2EB), // Used for the timer progress
    onPrimary = Color(0xFF0D0D0D), // Used for text over primary (e.g., Start Session)
    accent = Color(0xFFF27D26), // Shards orange
    inactive = Color(0xFFFFFFFF).copy(alpha = 0.1f) // text-white/10
)

val ForestGreenColors = SiloColors(
    background = Color(0xFF101B17),
    surface = Color(0xFF1A2A24),
    onBackground = Color(0xFFEAE6DF),
    onSurface = Color(0xFFD2CEC4),
    primary = Color(0xFF8FBC8F), // Sage Spruce green
    onPrimary = Color(0xFF101B17),
    accent = Color(0xFF5D8F5D),
    inactive = Color(0xFF2B3D36)
)

val MatchaColors = SiloColors(
    background = Color(0xFF1B231D),
    surface = Color(0xFF26332A),
    onBackground = Color(0xFFE6EDE8),
    onSurface = Color(0xFFD1DDD4),
    primary = Color(0xFF8EB594), // Matcha powder leaf
    onPrimary = Color(0xFF1B231D),
    accent = Color(0xFF6B8B70),
    inactive = Color(0xFF35443B)
)

val CyberpunkNightColors = SiloColors(
    background = Color(0xFF03030F),
    surface = Color(0xFF0C0C24),
    onBackground = Color(0xFFE0E0FF),
    onSurface = Color(0xFFB5B5E6),
    primary = Color(0xFF00FFCC), // Glowing digital cyan
    onPrimary = Color(0xFF03030F),
    accent = Color(0xFFFF0066), // Grid laser magenta
    inactive = Color(0xFF1A1A40)
)

val MonoLightColors = SiloColors(
    background = Color(0xFFF6F6F6),
    surface = Color(0xFFE8E8E8),
    onBackground = Color(0xFF121212), // Deep carbon ink
    onSurface = Color(0xFF3A3A3A),
    primary = Color(0xFF121212),
    onPrimary = Color(0xFFF6F6F6),
    accent = Color(0xFF666666),
    inactive = Color(0xFFCCCCCC)
)

val LocalSiloColors = staticCompositionLocalOf { CharcoalZenColors }

@Composable
fun SiloTheme(themeName: String, content: @Composable () -> Unit) {
    val colors = when (themeName) {
        "Forest Green" -> ForestGreenColors
        "Matcha" -> MatchaColors
        "Cyberpunk Night" -> CyberpunkNightColors
        "Mono Light" -> MonoLightColors
        else -> CharcoalZenColors
    }
    CompositionLocalProvider(LocalSiloColors provides colors) {
        content()
    }
}
