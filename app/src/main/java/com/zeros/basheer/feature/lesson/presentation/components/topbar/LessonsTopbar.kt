package com.zeros.basheer.feature.lesson.presentation.components.topbar


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.zeros.basheer.feature.lesson.presentation.components.foundation.formatLessonCount

/**
 * Top app bar for Lessons screen with integrated search functionality.
 *
 * Features:
 * - Subject name display
 * - Lesson count
 * - Search toggle
 * - Search input field (when active)
 * - Back navigation
 *
 * @param subjectName Name of the current subject
 * @param totalLessons Total number of lessons
 * @param searchQuery Current search query text
 * @param isSearchActive Whether search mode is active
 * @param onSearchQueryChange Callback when search query changes
 * @param onSearchToggle Callback to toggle search mode
 * @param onBack Callback for back navigation
 * @param modifier Standard modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsTopBar(
    subjectName: String,
    totalLessons: Int,
    searchQuery: String,
    isSearchActive: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier.semantics {
            contentDescription = if (isSearchActive) {
                "Search lessons"
            } else {
                "Lessons in $subjectName"
            }
        },
        title = {
            if (isSearchActive) {
                SearchTextField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange
                )
            } else {
                LessonsTitle(
                    subjectName = subjectName,
                    totalLessons = totalLessons
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع"
                )
            }
        },
        actions = {
            IconButton(onClick = onSearchToggle) {
                Icon(
                    imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (isSearchActive) "إغلاق البحث" else "بحث"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

// ============================================================================
// INTERNAL COMPONENTS
// ============================================================================

/**
 * Title section showing subject name and lesson count
 */
@Composable
private fun LessonsTitle(
    subjectName: String,
    totalLessons: Int
) {
    Column {
        Text(
            text = if (subjectName.isNotEmpty()) subjectName else "الدروس",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        if (totalLessons > 0) {
            Text(
                text = formatLessonCount(totalLessons),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Search text field for entering search queries
 */
@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("ابحث عن درس...") },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}