package com.zeros.basheer.ui.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.zeros.basheer.feature.concept.domain.model.Concept
import com.zeros.basheer.feature.concept.domain.model.ConceptType

/**
 * Modal bottom sheet displaying concept details.
 * 
 * Future enhancements:
 * - Show content variants
 * - Show related concepts
 * - "Mark as understood" action
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConceptModal(
    concept: Concept?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (concept == null) return
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Type badge
                    ConceptTypeBadge(type = concept.type)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Title
                    Text(
                        text = concept.titleAr,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // English title if available
                    concept.titleEn?.let { en ->
                        Text(
                            text = en,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Image if available
            concept.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = concept.titleAr,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Definition
            Text(
                text = concept.definition,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Formula if available
            concept.formula?.let { formula ->
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "القانون",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formula,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Difficulty indicator
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الصعوبة:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                repeat(5) { index ->
                    val isActive = index < concept.difficulty
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = MaterialTheme.shapes.small,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    ) {}
                }
            }
            
            // Future: Show variants button
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedButton(
                onClick = { /* TODO: Show variants */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = false // Enable when variants are implemented
            ) {
                Text("عرض الشروحات البديلة")
            }
        }
    }
}

@Composable
private fun ConceptTypeBadge(type: ConceptType) {
    val (label, color) = when (type) {
        ConceptType.DEFINITION -> "تعريف" to MaterialTheme.colorScheme.primary
        ConceptType.FORMULA -> "قانون" to MaterialTheme.colorScheme.secondary
        ConceptType.DATE -> "تاريخ" to MaterialTheme.colorScheme.tertiary
        ConceptType.PERSON -> "شخصية" to MaterialTheme.colorScheme.secondary
        ConceptType.LAW -> "قاعدة" to MaterialTheme.colorScheme.primary
        ConceptType.FACT -> "حقيقة" to MaterialTheme.colorScheme.tertiary
        ConceptType.PROCESS -> "عملية" to MaterialTheme.colorScheme.secondary
        ConceptType.COMPARISON -> "مقارنة" to MaterialTheme.colorScheme.tertiary
        ConceptType.PLACE -> "مكان" to MaterialTheme.colorScheme.primary
        ConceptType.CAUSE_EFFECT -> "سبب ونتيجة" to MaterialTheme.colorScheme.secondary
    }
    
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
