package com.example.anilist.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.anilist.R


object AniListRoute {
    const val HOME_ROUTE = "Home"
    const val ANIME_DETAIL_ROUTE = "Detail"
    const val ANIME_ROUTE = "Anime"
    const val MANGA_ROUTE = "Manga"
    const val FEED_ROUTE = "Feed"
    const val FORUM_ROUTE = "Forum"
}

data class AnilistTopLevelDestination(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int
)

class AniListNavigationActions(private val navController: NavController) {
    fun navigateTo(destination: AnilistTopLevelDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }

            launchSingleTop = true

            restoreState = true
        }
    }
}

val TOP_LEVEL_DESTINATIONS = listOf(
    AnilistTopLevelDestination(
        route = AniListRoute.HOME_ROUTE,
        selectedIcon = Icons.Default.Home,
        unselectedIcon = Icons.Default.Home,
        iconTextId = R.string.home
    ),
    AnilistTopLevelDestination(
        route = AniListRoute.ANIME_ROUTE,
        selectedIcon = Icons.Default.Star,
        unselectedIcon = Icons.Default.Star,
        iconTextId = R.string.anime
    ),
    AnilistTopLevelDestination(
        route = AniListRoute.MANGA_ROUTE,
        selectedIcon = Icons.Default.Star,
        unselectedIcon = Icons.Default.Star,
        iconTextId = R.string.manga
    ),
    AnilistTopLevelDestination(
        route = AniListRoute.FEED_ROUTE,
        selectedIcon = Icons.Default.Star,
        unselectedIcon = Icons.Default.Star,
        iconTextId = R.string.feed
    ),
    AnilistTopLevelDestination(
        route = AniListRoute.FORUM_ROUTE,
        selectedIcon = Icons.Default.Star,
        unselectedIcon = Icons.Default.Star,
        iconTextId = R.string.forum
    ),
)