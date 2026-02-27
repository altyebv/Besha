package com.zeros.basheer.feature.user.presentation.onboarding.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.analytics.domain.model.AnalyticsConsent
import kotlinx.coroutines.delay

@Composable
fun ConsentStep(
    isSaving: Boolean,
    onConsentChosen: (AnalyticsConsent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showContent by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<AnalyticsConsent?>(null) }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // ── Header ────────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically { -20 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🌍", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "ساعدنا نبني بشير أحسن",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "التطبيق يشتغل كامل بدون نت\nاختار كيف تحب تساعدنا",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Consent cards ─────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically { 24 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                ConsentCard(
                    isSelected = selected == AnalyticsConsent.FULL,
                    onClick = { selected = AnalyticsConsent.FULL },
                    icon = Icons.Default.Person,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "شارك مع سياق",
                    subtitle = "السلوك + الولاية والمدرسة والتخصص",
                    detail = "بيساعدنا نحسّن المحتوى لكل منطقة ومدرسة",
                    badge = "الأفيد للتطوير",
                    badgeColor = MaterialTheme.colorScheme.primary,
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    privacyNote = "اسمك وبريدك ما بيطلعوا أبداً"
                )

                ConsentCard(
                    isSelected = selected == AnalyticsConsent.ANONYMOUS,
                    onClick = { selected = AnalyticsConsent.ANONYMOUS },
                    icon = Icons.Default.BarChart,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "مجهول",
                    subtitle = "سلوك الاستخدام فقط",
                    detail = "الدروس، التمارين، والوقت — بدون أي بيانات شخصية",
                    badge = null,
                    badgeColor = Color.Unspecified,
                    selectedBorderColor = MaterialTheme.colorScheme.secondary,
                    privacyNote = "ما في شيء يربطك بالبيانات"
                )

                ConsentCard(
                    isSelected = selected == AnalyticsConsent.NONE,
                    onClick = { selected = AnalyticsConsent.NONE },
                    icon = Icons.Default.Block,
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    title = "لا تشارك أي شيء",
                    subtitle = "التطبيق بيشتغل عادي",
                    detail = "ما راح نجمع أي بيانات منك",
                    badge = null,
                    badgeColor = Color.Unspecified,
                    selectedBorderColor = MaterialTheme.colorScheme.outline,
                    privacyNote = null
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(visible = showContent, enter = fadeIn()) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "البيانات مشفرة ومحدودة القراءة — التطبيق فقط بيكتب، ما بيقرأ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically { 32 }
        ) {
            Button(
                onClick = { selected?.let { onConsentChosen(it) } },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = selected != null && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (selected == null) "اختار خيار للمتابعة" else "متابعة  →",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun ConsentCard(
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    detail: String,
    badge: String?,
    badgeColor: Color,
    selectedBorderColor: Color,
    privacyNote: String?,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) selectedBorderColor else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(200), label = "border"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) selectedBorderColor.copy(alpha = 0.07f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200), label = "bg"
    )

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null,
                        tint = iconTint, modifier = Modifier.size(22.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    if (badge != null) {
                        Surface(shape = RoundedCornerShape(6.dp),
                            color = badgeColor.copy(alpha = 0.15f)) {
                            Text(text = badge, style = MaterialTheme.typography.labelSmall,
                                color = badgeColor, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = detail, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                if (privacyNote != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Text(text = privacyNote, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (isSelected) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null,
                    tint = selectedBorderColor,
                    modifier = Modifier.size(22.dp).align(Alignment.CenterVertically))
            }
        }
    }
}