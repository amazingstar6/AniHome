package com.example.anilist.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.anilist.R
import com.example.anilist.data.models.HomeTrendingTypes

object AniListRoute {
    const val THREAD_COMMENT: String = "ThreadComment"
    const val ACTIVITY: String = "Activity"
    const val MEDIA_OVERVIEW_ROUTE: String = "MediaOverviewRoute"
    const val CHARACTER_DETAIL_ROUTE: String = "CharacterDetail"
    const val STAFF_DETAIL_ROUTE: String = "StaffDetail"
    const val REVIEW_DETAIL_ROUTE: String = "ReviewDetail"
    const val STUDIO_DETAIL_ROUTE: String = "StudioDetail"
    const val THREAD_DETAIL_ROUTE: String = "ThreadDetail"
    const val USER_DETAIL_ROUTE: String = "UserDetail"
    const val COVER_LARGE: String = "CoverLarge"
    const val SETTINGS: String = "Settings"
    const val NOTIFICATION_ROUTE: String = "Notification"
    const val HOME_ROUTE = "Home"
    const val MEDIA_DETAIL_ROUTE = "Detail"
    const val ANIME_ROUTE = "Anime"
    const val MANGA_ROUTE = "Manga"
    const val FEED_ROUTE = "Feed"
    const val FORUM_ROUTE = "Forum"
    const val MEDIA_DETAIL_ID_KEY = "mediaId"
    const val STAFF_DETAIL_ID_KEY = "staffId"
    const val CHARACTER_DETAIL_KEY = "characterId"
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
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }

    fun navigateToMediaDetails(mediaId: Int) {
        navController.navigate(
            route = AniListRoute.MEDIA_DETAIL_ROUTE + "/$mediaId",
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
            route = AniListRoute.STUDIO_DETAIL_ROUTE + "/$id",
        )
    }

    fun navigateToThread(id: Int) {
        navController.navigate(
            route = AniListRoute.THREAD_DETAIL_ROUTE + "/$id",
        )
    }

    fun navigateToUser(id: Int) {
        navController.navigate(
            route = AniListRoute.USER_DETAIL_ROUTE + "/$id",
        )
    }

    fun navigateToLargeCover(imageString: String) {
        navController.navigate(
            route = AniListRoute.COVER_LARGE + "/$imageString",
        )
    }

    fun navigateToOverview(homeTrendingTypes: HomeTrendingTypes) {
        navController.navigate(
            route = AniListRoute.MEDIA_OVERVIEW_ROUTE + "/${homeTrendingTypes.ordinal}",
        )
    }

    fun navigateToActivity(activityId: Int) {
        navController.navigate(
            route = AniListRoute.ACTIVITY + "/$activityId",
        )
    }

    fun navigateToThreadComment(commentId: Int) {
        navController.navigate(
            route = AniListRoute.THREAD_COMMENT + "/$commentId",
        )
    }
}

/**
 * Describes all the top level destinations in the app; the three destinations in the bottom bar
 */
val TOP_LEVEL_DESTINATIONS =
    listOf(
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
            iconTextId = R.string.my_anime,
        ),
        AnilistTopLevelDestination(
            route = AniListRoute.MANGA_ROUTE,
            selectedIcon = R.drawable.navigation_manga_filled,
            unselectedIcon = R.drawable.navigation_manga_outlined,
            iconTextId = R.string.my_manga,
        ),
//    AnilistTopLevelDestination(
//        route = AniListRoute.FEED_ROUTE,
//        selectedIcon = R.drawable.navigation_feed_filled,
//        unselectedIcon = R.drawable.navigation_feed_outlined,
//        iconTextId = R.string.feed,
//    ),
//    AnilistTopLevelDestination(
//        route = AniListRoute.FORUM_ROUTE,
//        selectedIcon = R.drawable.navigation_forum_filled,
//        unselectedIcon = R.drawable.navigation_forum_outlined,
//        iconTextId = R.string.forum,
//    ),
    )
