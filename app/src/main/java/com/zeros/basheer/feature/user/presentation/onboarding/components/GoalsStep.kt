package com.zeros.basheer.feature.user.presentation.onboarding.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class Major(val key: String, val emoji: String, val label: String)

private val MAJORS = listOf(
    Major("MEDICINE",    "🏥", "طب وصيدلة"),
    Major("ENGINEERING", "💻", "هندسة"),
    Major("CS",          "🖥️", "حاسوب"),
    Major("ECONOMICS",   "📊", "اقتصاد"),
    Major("LAW",         "⚖️", "قانون"),
    Major("EDUCATION",   "📚", "تعليم"),
    Major("AGRICULTURE", "🌾", "زراعة"),
    Major("SCIENCE",     "🔬", "علوم"),
    Major("UNDECIDED",   "✨", "لم أقرر")
)

@Composable
fun GoalsStep(
    selectedMajor: String?,
    onMajorSelected: (String?) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "ما هدفك المستقبلي؟",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "نخصص المحتوى بما يناسب مسارك",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(MAJORS) { major ->
                val isSelected = selectedMajor == major.key
                val containerColor = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant

                Card(
                    onClick = {
                        // UNDECIDED deselects others; selecting other clears UNDECIDED
                        if (major.key == "UNDECIDED") {
                            onMajorSelected(if (isSelected) null else "UNDECIDED")
                        } else {
                            onMajorSelected(if (isSelected) null else major.key)
                        }
                    },
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = major.emoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = major.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) { Text("رجوع") }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(2f).height(56.dp),
                shape = MaterialTheme.shapes.medium
                // always enabled — goal selection is optional
            ) {
                Text(
                    text = "التالي",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}