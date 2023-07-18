package com.example.anilist.ui.navigation

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
    val selectedIcon: Int,
    val unselectedIcon: Int,
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
        selectedIcon = R.drawable.navigation_home_filled,
        unselectedIcon = R.drawable.navigation_home_outlined,
        iconTextId = R.string.home
    ),
    AnilistTopLevelDestination(
        route = AniListRoute.ANIME_ROUTE,
        selectedIcon = R.drawable.navigation_anime_filled,
        unselectedIcon = R.drawable.navigation_anime_outlined,
        iconTextId = R.string.anime
    ),
    AnilistTopLevelDestination(
        route = AniListRoute.MANGA_ROUTE,
        selectedIcon = R.drawable.navigation_manga_filled,
        unselectedIcon = R.drawable.navigation_manga_outlined,
        iconTextId = R.string.manga
    ),
    AnilistTopLevelDestination(
        route = AniListRoute.FEED_ROUTE,
        selectedIcon = R.drawable.navigation_feed_filled,
        unselectedIcon = R.drawable.navigation_feed_outlined,
        iconTextId = R.string.feed
    ),
    AnilistTopLevelDestination(
        route = AniListRoute.FORUM_ROUTE,
        selectedIcon = R.drawable.navigation_forum_filled,
        unselectedIcon = R.drawable.navigation_forum_outlined,
        iconTextId = R.string.forum
    ),
)