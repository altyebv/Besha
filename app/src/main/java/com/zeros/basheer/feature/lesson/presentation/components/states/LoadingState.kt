package com.zeros.basheer.feature.lesson.presentation.components.states


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Standard loading state component.
 * Centers a circular progress indicator.
 */
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String = "Loading..."
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = message
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}