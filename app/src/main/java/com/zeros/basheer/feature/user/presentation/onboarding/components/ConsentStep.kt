package com.zeros.basheer.feature.user.presentation.onboarding.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ConsentStep(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Stagger the content in for a polished feel
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        // Icon
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically { -20 }
        ) {
            Text(text = "🌍", fontSize = 64.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Headline
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically { -16 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ساعدنا نبني بشير أحسن",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "التطبيق يشتغل كامل بدون نت —\nبس لو سمحت شارك بياناتك معانا",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // What we collect card
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically { 24 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "شنو بنجمع؟",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ConsentDataPoint(
                    icon = Icons.Default.Book,
                    title = "الدروس والتمارين",
                    description = "أي دروس اتفتحت ونتائج التمارين"
                )
                ConsentDataPoint(
                    icon = Icons.Default.BarChart,
                    title = "تقدمك في المذاكرة",
                    description = "كم سؤال صح وكم غلط، وفي أي مادة"
                )
                ConsentDataPoint(
                    icon = Icons.Default.Timer,
                    title = "وقت الاستخدام",
                    description = "كم دقيقة بتذاكر في اليوم"
                )
                ConsentDataPoint(
                    icon = Icons.Default.Lock,
                    title = "مافي بيانات شخصية",
                    description = "اسمك ومعلوماتك ما بتطلع من جهازك أبداً"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Privacy note
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn()
        ) {
            Text(
                text = "البيانات بتتجمع بشكل مجهول وبتساعدنا نعرف أي جزء في التطبيق محتاج تحسين",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // CTA buttons
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically { 32 }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary — accept
                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = "✓  أوافق وأساعد في التطوير",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Secondary — decline (deliberately understated)
                TextButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Text(
                        text = "لا، شكراً",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ConsentDataPoint(
    icon: ImageVector,
    title: String,
    description: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(20.dp)
                .offset(y = 2.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}