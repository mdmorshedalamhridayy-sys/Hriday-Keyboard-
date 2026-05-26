package com.example.ui.theme

import androidx.compose.ui.graphics.Color

data class KeyboardThemeColors(
    val background: Color,
    val keyBackground: Color,
    val keyTextColor: Color,
    val specialKeyBackground: Color,
    val specialKeyTextColor: Color,
    val accentColor: Color,
    val activeBorderColor: Color,
    val cardBackground: Color
)

object KeyboardThemes {
    val ElegantDark = KeyboardThemeColors(
        background = Color(0xFF1C1B1F),
        keyBackground = Color(0xFF36343B),
        keyTextColor = Color(0xFFE6E1E5),
        specialKeyBackground = Color(0xFF49454F),
        specialKeyTextColor = Color(0xFFD0BCFF),
        accentColor = Color(0xFFD0BCFF),
        activeBorderColor = Color(0xFF4A4458),
        cardBackground = Color(0xFF2B2930)
    )

    val SpaceViolet = KeyboardThemeColors(
        background = Color(0xFF0F071D),
        keyBackground = Color(0xFF23143D),
        keyTextColor = Color(0xFFEADBFF),
        specialKeyBackground = Color(0xFF381C5C),
        specialKeyTextColor = Color(0xFFD3A4FF),
        accentColor = Color(0xFFFF3366),
        activeBorderColor = Color(0xFF9E5EFF),
        cardBackground = Color(0xFF1B0B30)
    )

    val NeonCyber = KeyboardThemeColors(
        background = Color(0xFF000000),
        keyBackground = Color(0xFF111111),
        keyTextColor = Color(0xFF00FFCC),
        specialKeyBackground = Color(0xFF222222),
        specialKeyTextColor = Color(0xFFFF007F),
        accentColor = Color(0xFFFF00F0),
        activeBorderColor = Color(0xFF00FFFF),
        cardBackground = Color(0xFF151515)
    )

    val GoldenLuxury = KeyboardThemeColors(
        background = Color(0xFF121212),
        keyBackground = Color(0xFF1E1E1E),
        keyTextColor = Color(0xFFD4AF37),
        specialKeyBackground = Color(0xFF2B2B2B),
        specialKeyTextColor = Color(0xFFF3E5AB),
        accentColor = Color(0xFFFFD700),
        activeBorderColor = Color(0xFFC5A059),
        cardBackground = Color(0xFF1F1F1F)
    )

    val PastelBlossom = KeyboardThemeColors(
        background = Color(0xFFFFF0F5),
        keyBackground = Color(0xFFFFF9FA),
        keyTextColor = Color(0xFF7D5260),
        specialKeyBackground = Color(0xFFFCE4EC),
        specialKeyTextColor = Color(0xFFC2185B),
        accentColor = Color(0xFFEC407A),
        activeBorderColor = Color(0xFFF48FB1),
        cardBackground = Color(0xFFFFF5F7)
    )

    val CrimsonDark = KeyboardThemeColors(
        background = Color(0xFF0D0303),
        keyBackground = Color(0xFF1C0A0A),
        keyTextColor = Color(0xFFFFA3A3),
        specialKeyBackground = Color(0xFF341111),
        specialKeyTextColor = Color(0xFFFF4D4D),
        accentColor = Color(0xFFFF2222),
        activeBorderColor = Color(0xFFFF0000),
        cardBackground = Color(0xFF1A0909)
    )

    val ClassicSlate = KeyboardThemeColors(
        background = Color(0xFFF1F3F4),
        keyBackground = Color(0xFFFFFFFF),
        keyTextColor = Color(0xFF202124),
        specialKeyBackground = Color(0xFFDADCE0),
        specialKeyTextColor = Color(0xFF1A73E8),
        accentColor = Color(0xFF1A73E8),
        activeBorderColor = Color(0xFFBDC1C6),
        cardBackground = Color(0xFFF8F9FA)
    )

    val ALL_THEMES = listOf(
        "Elegant Dark" to ElegantDark,
        "Space Violet" to SpaceViolet,
        "Neon Cyber" to NeonCyber,
        "Golden Luxury" to GoldenLuxury,
        "Pastel Blossom" to PastelBlossom,
        "Crimson Dark" to CrimsonDark,
        "Classic Slate" to ClassicSlate
    )

    fun getByName(name: String?): KeyboardThemeColors {
        return ALL_THEMES.firstOrNull { it.first.equals(name, ignoreCase = true) }?.second ?: ElegantDark
    }
}
