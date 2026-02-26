package com.zeros.basheer.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.user.domain.model.UserProfile
import com.zeros.basheer.feature.user.domain.model.XpSummary

@Composable
fun ProfileHeroHeader(
    profile: UserProfile?,
    xpSummary: XpSummary?,
    currentStreak: Int,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pathLabel = when (profile?.studentPath) {
        StudentPath.SCIENCE  -> "المسار العلمي"
        StudentPath.LITERARY -> "المسار الأدبي"
        StudentPath.COMMON   -> "المسار المشترك"
        null                 -> "طالب الشهادة السودانية"
    }

    val initials = profile?.name
        ?.split(" ")
        ?.take(2)
        ?.mapNotNull { it.firstOrNull()?.toString() }
        ?.joinToString("")
        ?: "ب"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFF59E0B), Color(0xFFF97316))
                )
            )
    ) {
        // Decorative background circles
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = 30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = ProfileMetrics.screenPadding)
                .padding(
                    top = ProfileMetrics.heroVerticalPadding,
                    bottom = 28.dp
                ),
            verticalArrangement = Arrangement.spacedBy(ProfileMetrics.sectionSpacing)
        ) {
            // ── Top row: title + edit button ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "حسابي",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.clickable { onEditClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = ProfileMetrics.chipVertical
                        ),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "تعديل الملف",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "تعديل",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // ── Avatar + identity ─────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(ProfileMetrics.avatarSize)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = profile?.name ?: "بشير",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = pathLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }
            }

            // ── Stat chips row ────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroStatChip(
                    icon = Icons.Default.LocalFireDepartment,
                    value = "$currentStreak",
                    label = "يوم"
                )
                xpSummary?.let { xp ->
                    HeroStatChip(
                        icon = Icons.Outlined.Star,
                        value = "${xp.totalXp}",
                        label = "XP"
                    )
                    HeroStatChip(
                        icon = Icons.Outlined.MilitaryTech,
                        value = "م${xp.level}",
                        label = "المستوى"
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HERO STAT CHIP — reusable inside the header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HeroStatChip(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = Color.White.copy(alpha = 0.18f)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = ProfileMetrics.chipHorizontal,
                vertical = ProfileMetrics.chipVertical
            ),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "$value $label",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}