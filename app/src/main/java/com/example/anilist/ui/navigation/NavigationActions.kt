package com.example.anilist.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.anilist.R

object AniListRoute {
    const val CHARACTER_DETAIL_ROUTE: String = "CharacterDetail"
    const val STAFF_DETAIL_ROUTE: String = "StaffDetail"
    const val REVIEW_DETAIL_ROUTE: String = "ReviewDetail"
    const val STUDIO_DETAIL_ROUTE: String = "StudioDetail"
    const val THREAD_DETAIL_ROUTE: String = "ThreadDetail"
    const val USER_DETAIL_ROUTE: String = "UserDetail"
    const val COVER_LARGE: String = "CoverLarge"
    const val STATUS_EDITOR: String = "StatusEditor"
    const val SETTINGS: String = "Settings"
    const val NOTIFICATION_ROUTE: String = "Notification"
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
    val iconTextId: Int,
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

    fun navigateToMediaDetails(mediaId: Int) {
        navController.navigate(
            route = AniListRoute.ANIME_DETAIL_ROUTE + "/$mediaId",
        )
    }

    fun navigateToReviewDetails(reviewId: Int) {
        navController.navigate(
            route = AniListRoute.REVIEW_DETAIL_ROUTE + "/$reviewId",
        )
    }

    fun navigateToCharacter(id: Int) {
        navController.navigate(
            route = AniListRoute.CHARACTER_DETAIL_ROUTE + "/$id",
        )
    }

    fun navigateToStaff(id: Int) {
        navController.navigate(
            route = AniListRoute.STAFF_DETAIL_ROUTE + "/$id",
        )
    }

    fun navigateBack() {
        navController.popBackStack()
    }

    fun navigateToStudio(id: Int) {
        navController.navigate(
            route = AniListRoute.STUDIO_DETAIL_ROUTE + "/$id"
        )
    }

    fun navigateToThread(id: Int) {
        navController.navigate(
            route = AniListRoute.THREAD_DETAIL_ROUTE + "/$id"
        )
    }

    fun navigateToUser(id: Int) {
        navController.navigate(
            route = AniListRoute.USER_DETAIL_ROUTE + "/$id"
        )
    }

    fun navigateToLargeCover(imageString: String) {
        navController.navigate(
            route = AniListRoute.COVER_LARGE + "/$imageString"
        )
    }

    fun navigateToStatusEditor(id: Int) {
        navController.navigate(
            route = AniListRoute.STATUS_EDITOR + "/$id"
        )
    }
}

val TOP_LEVEL_DESTINATIONS = listOf(
    AnilistTopLevelDestination(
        route = AniListRoute.HOME_ROUTE,
        selectedIcon = R.drawable.navigation_home_filled,
        unselectedIcon = R.drawable.navigation_home_outlined,
        iconTextId = R.string.home,
    ),
    AnilistTopLevelDestination(
        route = AniListRoute.ANIME_ROUTE,
        selectedIcon = R.drawable.navigation_anime_filled,
        unselectedIcon = R.drawable.navigation_anime_outlined,
        iconTextId = R.string.anime,
    ),
    AnilistTopLevelDestination(
        route = AniListRoute.MANGA_ROUTE,
        selectedIcon = R.drawable.navigation_manga_filled,
        unselectedIcon = R.drawable.navigation_manga_outlined,
        iconTextId = R.string.manga,
    ),
    AnilistTopLevelDestination(
        route = AniListRoute.FEED_ROUTE,
        selectedIcon = R.drawable.navigation_feed_filled,
        unselectedIcon = R.drawable.navigation_feed_outlined,
        iconTextId = R.string.feed,
    ),
    AnilistTopLevelDestination(
        route = AniListRoute.FORUM_ROUTE,
        selectedIcon = R.drawable.navigation_forum_filled,
        unselectedIcon = R.drawable.navigation_forum_outlined,
        iconTextId = R.string.forum,
    ),
)
