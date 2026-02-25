package com.zeros.basheer.ui.components.feeds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.feed.domain.model.FeedItemType
import com.zeros.basheer.feature.feed.domain.model.toBadge

/**
 * Top bar for feed cards — dark-first, subject-colored accent dot, emoji + name, type badge.
 */
@Composable
fun FeedCardTopBar(
    subjectName: String,
    feedType: FeedItemType,
    subjectColor: Color,
    subjectEmoji: String,
    modifier: Modifier = Modifier
) {
    val badge = feedType.toBadge()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Subject identity — colored dot + emoji + name
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(subjectColor)
            )
            Text(
                text = subjectEmoji,
                fontSize = 16.sp
            )
            Text(
                text = subjectName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.75f)
            )
        }

        // Type badge — pill with subject color tint for game types, subtle white for info
        Surface(
            shape = MaterialTheme.shapes.small,
            color = if (badge.isGame) subjectColor.copy(alpha = 0.25f)
            else Color.White.copy(alpha = 0.10f),
        ) {
            Text(
                text = badge.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (badge.isGame) subjectColor
                else Color.White.copy(alpha = 0.60f),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}