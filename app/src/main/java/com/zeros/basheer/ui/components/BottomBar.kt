package com.zeros.basheer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zeros.basheer.core.ui.theme.Secondary
import com.zeros.basheer.ui.navigation.Screen

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isFeatured: Boolean = false          // Special treatment for Feeds
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Main.route,
        label = "الرئيسية",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = Screen.Lessons.baseRoute,
        label = "الدروس",
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook
    ),
    BottomNavItem(
        route = Screen.Feeds.route,
        label = "مراجعة",
        selectedIcon = Icons.Filled.PlayCircle,
        unselectedIcon = Icons.Outlined.PlayCircle,
        isFeatured = true                    // ← This tab gets the spotlight
    ),
    BottomNavItem(
        // Must use baseRoute ("quizbank"), NOT Screen.QuizBank.route which is the
        // template string "quizbank?subjectId={subjectId}". Navigating to the
        // template literal causes SavedStateHandle to receive "{subjectId}" as
        // the subjectId value, making every DB query return zero rows.
        route = Screen.QuizBank.baseRoute,
        label = "الامتحانات",
        selectedIcon = Icons.Filled.Quiz,
        unselectedIcon = Icons.Outlined.Quiz
    ),
    BottomNavItem(
        route = Screen.Profile.route,
        label = "حسابي",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)

/**
 * Basheer bottom navigation bar.
 *
 * The Feeds tab gets a floating pill treatment — a coral accent button
 * that visually pops above the bar, signalling "this is where the
 * dopamine loop lives." Inspired by Duolingo's Practice button.
 */
@Composable
fun BasheerBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute?.let { route ->
                route == item.route || route.startsWith(item.route.split("/").first())
            } ?: false

            if (item.isFeatured) {
                // ── Featured Feeds tab ─────────────────────────────────────
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { navigateTo(navController, item.route, currentRoute) },
                    icon = {
                        FeaturedTabIcon(
                            icon = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            isSelected = isSelected
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Secondary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color.Transparent    // We draw our own indicator
                    )
                )
            } else {
                // ── Standard tab ──────────────────────────────────────────
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { navigateTo(navController, item.route, currentRoute) },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}

/**
 * The featured Feeds tab icon — a coral-filled circle that stands out
 * from the flat bar. When selected, it glows with the secondary color.
 */
@Composable
private fun FeaturedTabIcon(icon: ImageVector, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) Secondary
                else Secondary.copy(alpha = 0.15f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = if (isSelected) Color.White else Secondary
        )
    }
}

private fun navigateTo(navController: NavController, route: String, currentRoute: String?) {
    if (currentRoute == route) return
    navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}