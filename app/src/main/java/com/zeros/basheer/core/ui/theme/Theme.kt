package com.zeros.basheer.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

// ============================================================================
// SHAPE SYSTEM — Rounded, friendly, Duolingo-inspired
// ============================================================================

val BasheerShapes = Shapes(
    // Extra Small — tags, chips, tiny badges
    extraSmall = RoundedCornerShape(8.dp),
    // Small — input fields, small cards
    small = RoundedCornerShape(12.dp),
    // Medium — standard cards, dialogs
    medium = RoundedCornerShape(16.dp),
    // Large — bottom sheets, large cards
    large = RoundedCornerShape(20.dp),
    // Extra Large — banners, hero cards, full-width sections
    extraLarge = RoundedCornerShape(24.dp)
)

// ============================================================================
// LIGHT COLOR SCHEME — Warm cream base, amber primary
// ============================================================================

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    tertiary = Tertiary,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,

    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = Color(0xFF9F1239),

    background = Background,
    onBackground = OnBackground,

    surface = Surface,
    onSurface = OnSurface,

    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,

    outline = Outline,
    outlineVariant = OutlineStrong,
)

// ============================================================================
// DARK COLOR SCHEME — Warm dark, amber glow
// ============================================================================

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = Color(0xFF78350F),      // Deep amber container
    onPrimaryContainer = Color(0xFFFEF3C7),

    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = Color(0xFF9F1239),
    onSecondaryContainer = Color(0xFFFFE4E6),

    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF0C4A6E),
    tertiaryContainer = Color(0xFF0C4A6E),
    onTertiaryContainer = Color(0xFFE0F2FE),

    error = ErrorDark,
    onError = Color(0xFF1C1917),
    errorContainer = Color(0xFF9F1239),
    onErrorContainer = Color(0xFFFFE4E6),

    background = BackgroundDark,
    onBackground = OnBackgroundDark,

    surface = SurfaceDark,
    onSurface = OnSurfaceDark,

    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,

    outline = OutlineDark,
    outlineVariant = Color(0xFF2A2720),
)

// ============================================================================
// THEME ENTRY POINT
// ============================================================================

@Composable
fun BasheerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,             // Disabled — preserve Basheer branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Force RTL for Arabic content
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = BasheerShapes,
            content = content
        )
    }
}