package com.zeros.basheer.ui.components.blocks.foundation


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized design tokens for block components.
 * Single source of truth for spacing, colors, and common patterns.
 */

// ============================================================================
// SPACING SYSTEM
// ============================================================================

object BlockSpacing {
    // Standard block padding
    val horizontal = 16.dp
    val vertical = 8.dp

    // Internal spacing
    val small = 4.dp
    val medium = 8.dp
    val large = 16.dp
    val extraLarge = 24.dp
    val huge = 32.dp

    // Specific use cases
    val sectionGap = 16.dp
    val listItemGap = 8.dp
    val cardPadding = 16.dp
    val iconSpacing = 8.dp
}

// ============================================================================
// COLOR SYSTEM
// ============================================================================

object BlockColors {
    /**
     * Success/completed state color
     */
    @Composable
    fun success() = Color(0xFF4CAF50)

    /**
     * Error/incorrect state color
     */
    @Composable
    fun error() = Color(0xFFF44336)

    /**
     * Warning state color
     */
    @Composable
    fun warning() = Color(0xFFFF9800)

    /**
     * Standard alpha for light container backgrounds
     */
    fun containerAlphaLight() = 0.3f

    /**
     * Standard alpha for borders
     */
    fun borderAlpha() = 0.5f

    /**
     * Standard alpha for disabled/de-emphasized elements
     */
    fun disabledAlpha() = 0.6f
}

// ============================================================================
// SIZE SYSTEM
// ============================================================================

object BlockSizes {
    // Icon sizes
    val iconSmall = 16.dp
    val iconMedium = 20.dp
    val iconLarge = 24.dp
    val iconHuge = 40.dp

    // Touch targets (accessibility)
    val minTouchTarget = 48.dp

    // Border widths
    val borderThin = 0.5.dp
    val borderNormal = 1.dp
    val borderThick = 2.dp

    // Indentation for nested items
    val indentationLevel = 16.dp

    // Table cells
    val tableCellMinWidth = 80.dp
    val tableCellMaxWidth = 200.dp
}

// ============================================================================
// TYPOGRAPHY MULTIPLIERS
// ============================================================================

object BlockTypography {
    /**
     * Line height multiplier for body text (relative to font size)
     */
    const val bodyLineHeightMultiplier = 1.6f

    /**
     * Line height multiplier for headings
     */
    const val headingLineHeightMultiplier = 1.3f
}

// ============================================================================
// MODIFIER EXTENSIONS (Standard Patterns)
// ============================================================================

/**
 * Standard container modifier for all blocks.
 * Provides consistent horizontal/vertical padding.
 */
fun Modifier.blockContainer() = this
    .fillMaxWidth()
    .padding(horizontal = BlockSpacing.horizontal, vertical = BlockSpacing.vertical)

/**
 * Makes an element clickable with proper accessibility support.
 *
 * @param conceptRef Optional concept ID - only clickable if not null
 * @param onClick Callback when clicked
 * @param contentDescription Accessibility label for screen readers
 */
fun Modifier.clickableConcept(
    conceptRef: String?,
    onClick: (String) -> Unit,
    contentDescription: String = "View concept details"
) = this.then(
    if (conceptRef != null) {
        Modifier
            .clickable(
                role = Role.Button,
                onClickLabel = contentDescription
            ) { onClick(conceptRef) }
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            }
    } else {
        Modifier
    }
)

/**
 * Standard padding for card/surface interiors
 */
fun Modifier.cardInterior() = this.padding(BlockSpacing.cardPadding)

/**
 * Calculates indentation based on nesting level.
 * Used for nested lists, headings, etc.
 */
fun Dp.atLevel(level: Int): Dp = this * level

/**
 * Standard spacing between list items
 */
fun Modifier.listItemSpacing() = this.padding(bottom = BlockSpacing.listItemGap)