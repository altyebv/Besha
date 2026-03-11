package com.zeros.basheer.core.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// BRAND IDENTITY — Warm Amber/Gold
// Knowledge, Achievement, Arabic Manuscript Heritage
// ============================================================================

// Primary — Amber Gold
val Primary = Color(0xFFF59E0B)
val PrimaryVariant = Color(0xFFD97706)
val PrimaryDeep = Color(0xFFB45309)

// Primary Containers
val PrimaryContainer = Color(0xFFFEF3C7)       // Warm cream tint
val OnPrimaryContainer = Color(0xFF78350F)     // Deep amber text

// Secondary — Coral/Rose for gamification pops
val Secondary = Color(0xFFF43F5E)
val SecondaryVariant = Color(0xFFE11D48)
val SecondaryContainer = Color(0xFFFFE4E6)
val OnSecondaryContainer = Color(0xFF9F1239)

// Tertiary — Sky blue for info/study accents
val Tertiary = Color(0xFF0EA5E9)
val TertiaryContainer = Color(0xFFE0F2FE)
val OnTertiaryContainer = Color(0xFF0C4A6E)

// Semantic Colors
val Success = Color(0xFF10B981)                // Emerald — correct answers, completion
val SuccessContainer = Color(0xFFD1FAE5)
val Warning = Color(0xFFF59E0B)                // Amber — same as primary
val Error = Color(0xFFEF4444)
val ErrorContainer = Color(0xFFFFE4E6)

// ============================================================================
// LIGHT THEME SURFACES
// ============================================================================
val Background = Color(0xFFFFFDF5)             // Warm cream — easy on the eyes
val Surface = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFF5F0E8)         // Warm gray
val SurfaceElevated = Color(0xFFFFFBF0)        // Slightly warmer surface

val OnPrimary = Color(0xFFFFFFFF)
val OnSecondary = Color(0xFFFFFFFF)
val OnBackground = Color(0xFF1C1917)           // Warm near-black
val OnSurface = Color(0xFF1C1917)
val OnSurfaceVariant = Color(0xFF6B6560)       // Warm medium gray
val OnSurfaceMuted = Color(0xFF9B9390)         // Muted text
val Outline = Color(0xFFE7E0D8)                // Warm outline
val OutlineStrong = Color(0xFFCDC5BB)

// ============================================================================
// DARK THEME
// ============================================================================
val PrimaryDark = Color(0xFFFBBF24)            // Brighter amber for dark mode
val PrimaryVariantDark = Color(0xFFF59E0B)
val SecondaryDark = Color(0xFFFB7185)          // Lighter coral

val BackgroundDark = Color(0xFF0F0D09)         // Very dark warm black
val SurfaceDark = Color(0xFF1C1A14)            // Dark warm surface
val SurfaceVariantDark = Color(0xFF2A271F)     // Dark elevated
val SurfaceElevatedDark = Color(0xFF23201A)

val OnPrimaryDark = Color(0xFF1C1917)
val OnSecondaryDark = Color(0xFFFFFFFF)
val OnBackgroundDark = Color(0xFFF5F0E8)
val OnSurfaceDark = Color(0xFFF5F0E8)
val OnSurfaceVariantDark = Color(0xFFB8B0A5)
val ErrorDark = Color(0xFFF87171)
val OutlineDark = Color(0xFF3A3528)

// ============================================================================
// STREAK SYSTEM
// ============================================================================
val StreakFlame = Color(0xFFF97316)            // Vivid orange
val StreakSpark = Color(0xFFFBBF24)            // Amber spark
val StreakCold = Color(0xFF94A3B8)             // Cool gray

// ============================================================================
// SUBJECT COLOR SYSTEM — Each subject has its own identity
// ============================================================================
val SubjectPhysics = Color(0xFF3B82F6)         // Electric Blue — waves & forces
val SubjectChemistry = Color(0xFF8B5CF6)       // Violet — elements & reactions
val SubjectGeography = Color(0xFF10B981)       // Emerald — land & nature
val SubjectArabic = Color(0xFFEC4899)          // Rose — language & poetry
val SubjectIslamic = Color(0xFF0EA5E9)         // Sky Blue — spiritual clarity
val SubjectMilitary = Color(0xFF64748B)        // Slate — discipline & structure
val SubjectMath = Color(0xFF6366F1)            // Indigo — logic & precision
val SubjectBiology = Color(0xFF22C55E)         // Green — life sciences
val SubjectHistory = Color(0xFFD97706)         // Amber — heritage (warm)
val SubjectEnglish = Color(0xFF14B8A6)         // Teal — global language

// Fallback palette for unassigned subjects
val SubjectPalette = listOf(
    Color(0xFF3B82F6),  // Blue
    Color(0xFF8B5CF6),  // Violet
    Color(0xFF10B981),  // Emerald
    Color(0xFFEC4899),  // Rose
    Color(0xFF0EA5E9),  // Sky
    Color(0xFFF97316),  // Orange
    Color(0xFF6366F1),  // Indigo
    Color(0xFF14B8A6),  // Teal
    Color(0xFF22C55E),  // Green
    Color(0xFFD97706),  // Amber-variant
)

// ============================================================================
// XP / GAMIFICATION COLORS
// ============================================================================
val XpGold = Color(0xFFF59E0B)
val XpSilver = Color(0xFF94A3B8)
val XpBronze = Color(0xFFD97706)
val XpPlatinum = Color(0xFF7C3AED)

// ============================================================================
// LESSON / READER UI — Semantic aliases
//
// These are NOT new colors. They are named aliases for existing brand tokens,
// added here so lesson components can use expressive names (Amber, AmberDeep)
// without re-declaring private vals in every file.
//
// Before: each file had `private val Amber = Color(0xFFF59E0B)` — now they
// just import from this file. One source of truth, zero duplication.
//
// Mapping:
//   Amber          → Primary            (#F59E0B)
//   AmberDeep      → OnPrimaryContainer (#78350F)  dark text on amber bg
//   AmberLight     → PrimaryContainer   (#FEF3C7)  light amber tint
//   AmberContainer → PrimaryContainer   (#FEF3C7)  same as AmberLight
//   Coral          → Secondary          (#F43F5E)  accent, finish button
// ============================================================================
val Amber          = Primary             // lesson progress, part tabs, CTA buttons
val AmberDeep      = OnPrimaryContainer  // dark text/icon on amber backgrounds
val AmberLight     = PrimaryContainer    // subtle amber fill
val AmberContainer = PrimaryContainer    // card container tint (same as AmberLight)
val Coral          = Secondary           // finish bar, gamification accents

// ============================================================================
// LEGACY ALIASES (for backward compatibility)
// ============================================================================
val Purple80 = PrimaryDark
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Primary
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Secondary

val Info = Tertiary
val OnError = Color(0xFFFFFFFF)