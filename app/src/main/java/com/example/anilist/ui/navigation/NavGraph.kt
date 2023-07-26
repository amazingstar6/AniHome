package com.example.anilist.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.anilist.data.repository.UserSettings
import com.example.anilist.ui.PleaseLogin
import com.example.anilist.ui.feed.FeedScreen
import com.example.anilist.ui.forum.ForumScreen
import com.example.anilist.ui.home.AniHomeViewModel
import com.example.anilist.ui.home.HomeScreen
import com.example.anilist.ui.home.NotificationScreen
import com.example.anilist.ui.home.SettingsScreen
import com.example.anilist.ui.mediadetails.CharacterDetailScreen
import com.example.anilist.ui.mediadetails.MediaDetail
import com.example.anilist.ui.mediadetails.ReviewDetailScreen
import com.example.anilist.ui.mediadetails.StaffDetailScreen
import com.example.anilist.ui.my_media.MyMediaScreen

private const val TAG = "AniNavGraph"

@Composable
fun AniNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    navigationActions: AniListNavigationActions,
    aniHomeViewModel: AniHomeViewModel,
    userSettings: UserSettings?,
    setBottomBarState: (Boolean) -> Unit,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = AniListRoute.HOME_ROUTE,
    ) {
        composable(AniListRoute.HOME_ROUTE) {
            setBottomBarState(true)
            HomeScreen(
                aniHomeViewModel = aniHomeViewModel,
                onNavigateToNotification = {
                    navController.navigate(route = AniListRoute.NOTIFICATION_ROUTE)
                },
                onNavigateToDetails = navigationActions::navigateToMediaDetails,
                onNavigateToSettings = {
                    navController.navigate(route = AniListRoute.SETTINGS)
                },
            )
        }
        composable(
            AniListRoute.ANIME_DETAIL_ROUTE + "/{animeId}",
            arguments = listOf(
                navArgument("animeId") {
                    type = NavType.IntType
                },
            ),
        ) { backStackEntry ->
            setBottomBarState(false)
            MediaDetail(
                mediaId = backStackEntry.arguments?.getInt("animeId") ?: -1,
                onNavigateBack = navigationActions::navigateBack,
                onNavigateToDetails = navigationActions::navigateToMediaDetails,
                onNavigateToReviewDetails = navigationActions::navigateToReviewDetails,
                navigateToStaff = navigationActions::navigateToStaff,
                navigateToCharacter = navigationActions::navigateToCharacter,
                onNavigateToStaff = navigationActions::navigateToStaff,
            )
        }
        composable(
            route = AniListRoute.CHARACTER_DETAIL_ROUTE + "/{characterId}",
            arguments = listOf(navArgument("characterId") { type = NavType.IntType }),
            content = { backStackEntry ->
                CharacterDetailScreen(
                    id = backStackEntry.arguments?.getInt("characterId") ?: -1,
                    onNavigateToMedia = navigationActions::navigateToMediaDetails,
                    onNavigateToStaff = navigationActions::navigateToStaff,
                    onNavigateBack = navigationActions::navigateBack,
                )
            },
        )
        composable(
            route = AniListRoute.STAFF_DETAIL_ROUTE + "/{staffId}",
            arguments = listOf(navArgument("staffId") { type = NavType.IntType }),
            content = { backStackEntry ->
                StaffDetailScreen(
                    id = backStackEntry.arguments?.getInt("staffId") ?: -1,
                    onNavigateToCharacter = navigationActions::navigateToCharacter,
                    onNavigateToMedia = navigationActions::navigateToMediaDetails,
                    onNavigateBack = navigationActions::navigateBack,
                )
            },
        )
        composable(
            route = AniListRoute.REVIEW_DETAIL_ROUTE + "/{reviewId}",
            arguments = listOf(navArgument("reviewId") { type = NavType.IntType }),
            content = { backStackEntry ->
                ReviewDetailScreen(
                    reviewId = backStackEntry.arguments?.getInt("reviewId") ?: -1,
                )
            },
        )
        composable(
            AniListRoute.NOTIFICATION_ROUTE,
        ) {
            setBottomBarState(false)
            NotificationScreen(
                aniHomeViewModel = aniHomeViewModel,
                { navController.popBackStack() },
            )
        }
        composable(
            AniListRoute.SETTINGS,
        ) {
            setBottomBarState(false)
            SettingsScreen()
        }
        composable(AniListRoute.ANIME_ROUTE) {
            setBottomBarState(true)
//            val trendingAnimeUiState by aniHomeViewModel.uiState.collectAsState()
            Log.i(TAG, "Access code in #3 is ${userSettings?.accessCode}")
            if (userSettings?.accessCode != "" && userSettings == null) {
                Log.i(TAG, "ACCESS CODE STORED IS ${userSettings?.accessCode}")
                MyMediaScreen(
                    navigateToDetails = { id ->
                        navController.navigate(route = AniListRoute.ANIME_DETAIL_ROUTE + "/$id")
                    },
                    isAnime = true,
                )
            } else {
                PleaseLogin()
            }
        }
        composable(AniListRoute.MANGA_ROUTE) {
            setBottomBarState(true)
            MyMediaScreen(
                navigateToDetails = { id ->
                    navController.navigate(route = AniListRoute.ANIME_DETAIL_ROUTE + "/$id")
                },
                isAnime = false,
            )
        }
        composable(AniListRoute.FEED_ROUTE) {
            setBottomBarState(true)
            FeedScreen()
        }
        composable(AniListRoute.FORUM_ROUTE) {
            setBottomBarState(true)
            ForumScreen()
        }
    }
}
