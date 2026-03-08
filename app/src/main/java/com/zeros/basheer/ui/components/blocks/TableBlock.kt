package com.zeros.basheer.ui.components.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zeros.basheer.domain.model.BlockMetadata
import com.zeros.basheer.domain.model.BlockUiModel

// ─────────────────────────────────────────────────────────────────────────────
// LAYOUT CONSTANTS
// ─────────────────────────────────────────────────────────────────────────────

// Below this column count, use fillMaxWidth + weight so the table always fills
// the reading line and columns are equal-width. Above it, each column gets a
// fixed minimum width and the whole table becomes horizontally scrollable.
private const val SCROLL_THRESHOLD_COLUMNS = 4

// Minimum cell width used only in scroll mode
private val CELL_MIN_WIDTH: Dp = 96.dp

// ─────────────────────────────────────────────────────────────────────────────
// ENTRY POINT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TableBlock(
    block: BlockUiModel,
    modifier: Modifier = Modifier
) {
    val tableData = block.metadata as? BlockMetadata.Table
    val headers   = tableData?.headers ?: emptyList()
    val rawRows   = tableData?.rows    ?: emptyList()

    if (headers.isEmpty() && rawRows.isEmpty()) {
        Text(
            text = block.content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier.padding(16.dp)
        )
        return
    }

    // Normalize: every row must have the same cell count as the header row.
    // Short rows are padded with empty strings; extra cells are dropped.
    val columnCount = headers.size.coerceAtLeast(rawRows.maxOfOrNull { it.size } ?: 0)
    val rows = rawRows.map { row ->
        List(columnCount) { i -> row.getOrElse(i) { "" } }
    }

    val useScrollMode = columnCount > SCROLL_THRESHOLD_COLUMNS

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Optional caption above the table
        if (!block.caption.isNullOrBlank()) {
            Text(
                text = block.caption,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                textAlign = TextAlign.Center
            )
        }

        // Table surface
        val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .border(1.dp, borderColor, MaterialTheme.shapes.small)
        ) {
            if (useScrollMode) {
                ScrollableTable(
                    headers     = headers,
                    rows        = rows,
                    columnCount = columnCount,
                    borderColor = borderColor
                )
            } else {
                FixedTable(
                    headers     = headers,
                    rows        = rows,
                    columnCount = columnCount,
                    borderColor = borderColor
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FIXED-WIDTH TABLE  (≤ SCROLL_THRESHOLD_COLUMNS)
// All cells use weight(1f) so columns are equal-width and always aligned.
// Text wraps freely — no overflow, no clipping.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FixedTable(
    headers: List<String>,
    rows: List<List<String>>,
    columnCount: Int,
    borderColor: Color
) {
    Column {
        if (headers.isNotEmpty()) {
            TableRow(
                cells       = headers,
                columnCount = columnCount,
                isHeader    = true,
                isEven      = false,
                borderColor = borderColor
            )
        }
        rows.forEachIndexed { index, row ->
            TableRow(
                cells       = row,
                columnCount = columnCount,
                isHeader    = false,
                isEven      = index % 2 == 1,
                borderColor = borderColor
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SCROLLABLE TABLE  (> SCROLL_THRESHOLD_COLUMNS)
// Each cell has a fixed minimum width; the table scrolls horizontally.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ScrollableTable(
    headers: List<String>,
    rows: List<List<String>>,
    columnCount: Int,
    borderColor: Color
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.horizontalScroll(scrollState)) {
        if (headers.isNotEmpty()) {
            ScrollableTableRow(
                cells       = headers,
                isHeader    = true,
                isEven      = false,
                borderColor = borderColor
            )
        }
        rows.forEachIndexed { index, row ->
            ScrollableTableRow(
                cells       = row,
                isHeader    = false,
                isEven      = index % 2 == 1,
                borderColor = borderColor
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ROW IMPLEMENTATIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Row for fixed-width table. Cells use [weight] so column widths are always
 * synchronized regardless of content length.
 * [IntrinsicSize.Max] ensures all cells in the row share the tallest height.
 */
@Composable
private fun TableRow(
    cells: List<String>,
    columnCount: Int,
    isHeader: Boolean,
    isEven: Boolean,
    borderColor: Color
) {
    val rowBg = rowBackground(isHeader = isHeader, isEven = isEven)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .background(rowBg)
    ) {
        val normalizedCells = List(columnCount) { i -> cells.getOrElse(i) { "" } }
        normalizedCells.forEachIndexed { colIndex, cell ->
            WeightedCell(
                text        = cell,
                isHeader    = isHeader,
                borderColor = borderColor,
                showStart   = colIndex > 0,
                modifier    = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Row for scrollable table. Each cell has a fixed [CELL_MIN_WIDTH].
 */
@Composable
private fun ScrollableTableRow(
    cells: List<String>,
    isHeader: Boolean,
    isEven: Boolean,
    borderColor: Color
) {
    val rowBg = rowBackground(isHeader = isHeader, isEven = isEven)

    Row(
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .background(rowBg)
    ) {
        cells.forEachIndexed { colIndex, cell ->
            FixedCell(
                text        = cell,
                isHeader    = isHeader,
                borderColor = borderColor,
                showStart   = colIndex > 0
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CELL IMPLEMENTATIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Cell for fixed-width table. Width is determined by the parent Row's weight.
 * A vertical divider on the start edge separates adjacent cells.
 * A horizontal divider on the top edge separates rows (except the first).
 */
@Composable
private fun WeightedCell(
    text: String,
    isHeader: Boolean,
    borderColor: Color,
    showStart: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .then(
                if (showStart) Modifier.border(
                    width = 0.5.dp,
                    color = borderColor,
                    shape = androidx.compose.foundation.shape.CircleShape
                ) else Modifier
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        CellText(text = text, isHeader = isHeader)
    }
}

/**
 * Cell for scrollable table. Fixed [CELL_MIN_WIDTH] ensures content never
 * compresses columns below a readable size.
 */
@Composable
private fun FixedCell(
    text: String,
    isHeader: Boolean,
    borderColor: Color,
    showStart: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .widthIn(min = CELL_MIN_WIDTH)
            .fillMaxHeight()
            .then(
                if (showStart) Modifier.border(
                    width = 0.5.dp,
                    color = borderColor,
                    shape = androidx.compose.foundation.shape.CircleShape
                ) else Modifier
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        CellText(text = text, isHeader = isHeader)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SHARED HELPERS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CellText(text: String, isHeader: Boolean) {
    Text(
        text = text,
        style = if (isHeader)
            MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        else
            MaterialTheme.typography.bodySmall,
        color = if (isHeader)
            MaterialTheme.colorScheme.onSurface
        else
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
        textAlign = TextAlign.Center,
        softWrap = true   // Explicit: always wrap, never clip
    )
}

@Composable
private fun rowBackground(isHeader: Boolean, isEven: Boolean): Color = when {
    isHeader -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    isEven   -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    else     -> Color.Transparent
}