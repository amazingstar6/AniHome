package com.example.anilist.ui.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.anilist.ui.ActivityDetailScreen
import com.example.anilist.ui.PleaseLogin
import com.example.anilist.ui.feed.FeedScreen
import com.example.anilist.ui.forum.ForumScreen
import com.example.anilist.ui.home.HomeScreen
import com.example.anilist.ui.home.HomeViewModel
import com.example.anilist.ui.home.MediaOverview
import com.example.anilist.ui.home.notifications.NotificationScreen
import com.example.anilist.ui.home.SettingsScreen
import com.example.anilist.ui.mediadetails.CharacterDetailScreen
import com.example.anilist.ui.mediadetails.CoverLarge
import com.example.anilist.ui.mediadetails.ForumDetailScreen
import com.example.anilist.ui.mediadetails.MediaDetail
import com.example.anilist.ui.mediadetails.ReviewDetailScreen
import com.example.anilist.ui.mediadetails.StaffDetailScreen
import com.example.anilist.ui.mediadetails.StudioDetailScreen
import com.example.anilist.ui.mediadetails.UserDetailScreen
import com.example.anilist.ui.mymedia.MyMediaScreen
import timber.log.Timber

private const val TAG = "AniNavGraph"

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AniNavHost(
    accessCode: String,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    navigationActions: AniListNavigationActions,
    setBottomBarState: (Boolean) -> Unit,
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = AniListRoute.HOME_ROUTE,
    ) {
        composable(AniListRoute.HOME_ROUTE) {
            setBottomBarState(true)
            HomeScreen(
                homeViewModel = homeViewModel,
                onNavigateToNotification = {
                    navController.navigate(route = AniListRoute.NOTIFICATION_ROUTE)
                },
                onNavigateToMediaDetails = navigationActions::navigateToMediaDetails,
                onNavigateToSettings = {
                    navController.navigate(route = AniListRoute.SETTINGS)
                },
                onNavigateToCharacterDetails = navigationActions::navigateToCharacter,
                onNavigateToStaffDetails = navigationActions::navigateToStaff,
                navigateToStudioDetails = navigationActions::navigateToStudio,
                navigateToThreadDetails = navigationActions::navigateToThread,
                navigateToUserDetails = navigationActions::navigateToUser,
                navigateToOverview = navigationActions::navigateToOverview
            )
        }
        composable(
            route = AniListRoute.MEDIA_OVERVIEW_ROUTE + "/{trendingType}",
            arguments = listOf(navArgument("trendingType") {
                type = NavType.IntType
            })
        ) { navBackStackEntry ->
            val ordinalNumber = navBackStackEntry.arguments?.getInt("trendingType") ?: -1
            setBottomBarState(false)
            MediaOverview(
                homeViewModel = homeViewModel,
                ordinalNumber = ordinalNumber,
                navigateBack = navigationActions::navigateBack,
                navigateToDetails = navigationActions::navigateToMediaDetails
            )
        }
        composable(
            "${AniListRoute.MEDIA_DETAIL_ROUTE}/{${AniListRoute.MEDIA_DETAIL_ID_KEY}}",
            arguments = listOf(
                navArgument(AniListRoute.MEDIA_DETAIL_ID_KEY) {
                    type = NavType.IntType
                },
            ),
        ) { backStackEntry ->
            setBottomBarState(false)
            MediaDetail(
                mediaId = backStackEntry.arguments?.getInt(AniListRoute.MEDIA_DETAIL_ID_KEY) ?: -1,
                onNavigateBack = navigationActions::navigateBack,
                onNavigateToDetails = navigationActions::navigateToMediaDetails,
                onNavigateToReviewDetails = navigationActions::navigateToReviewDetails,
                navigateToStaff = navigationActions::navigateToStaff,
                navigateToCharacter = navigationActions::navigateToCharacter,
                onNavigateToStaff = navigationActions::navigateToStaff,
                onNavigateToLargeCover = navigationActions::navigateToLargeCover,
                navigateToStudioDetails = navigationActions::navigateToStudio
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
                    onNavigateBack = navigationActions::navigateBack
                )
            },
        )
        composable(
            route = AniListRoute.STUDIO_DETAIL_ROUTE + "/{studioId}",
            arguments = listOf(navArgument("studioId") { type = NavType.IntType }),
            content = { backStackEntry ->
                StudioDetailScreen(
                    id = backStackEntry.arguments?.getInt("studioId") ?: -1,
                    navigateToMedia = navigationActions::navigateToMediaDetails,
                    navigateBack = navigationActions::navigateBack
                )
            },
        )
        composable(
            route = AniListRoute.THREAD_DETAIL_ROUTE + "/{forumId}",
            arguments = listOf(navArgument("forumId") { type = NavType.IntType }),
            content = { backStackEntry ->
                ForumDetailScreen(
                    id = backStackEntry.arguments?.getInt("forumId") ?: -1,
                )
            },
        )
        composable(
            route = AniListRoute.USER_DETAIL_ROUTE + "/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType }),
            content = { backStackEntry ->
                UserDetailScreen(
                    id = backStackEntry.arguments?.getInt("userId") ?: -1,
                )
            },
        )
        composable(route = AniListRoute.COVER_LARGE + "/{imageString}",
            arguments = listOf(navArgument("imageString") { type = NavType.StringType }),
            content = { navBackStackEntry ->
                CoverLarge(
                    coverImage = navBackStackEntry.arguments?.getString("imageString") ?: "",
                    navigateBack = navigationActions::navigateBack
                )
            })
        composable(
            AniListRoute.NOTIFICATION_ROUTE,
        ) {
            setBottomBarState(false)
            NotificationScreen(
                onNavigateBack = { navController.popBackStack() },
                navigateToMediaDetails = navigationActions::navigateToMediaDetails,
                onNavigateToActivity = navigationActions::navigateToActivity,
                onNavigateToUser = navigationActions::navigateToUser
            )
        }
        composable(
            route = AniListRoute.ACTIVITY + "/{activityId}",
            arguments = listOf(navArgument("activityId") { type = NavType.IntType }),
            content = { navBackStackEntry ->
                ActivityDetailScreen(
                    activityId = navBackStackEntry.arguments?.getInt("activityId") ?: -1,
                    navigateBack = navigationActions::navigateBack
                )
            })
        composable(
            AniListRoute.SETTINGS,
        ) {
            setBottomBarState(false)
            SettingsScreen(navigateBack = navigationActions::navigateBack)
        }
        composable(AniListRoute.ANIME_ROUTE) {
            setBottomBarState(true)
            Timber.d("Access code is " + accessCode)
            if (accessCode != "") {
                MyMediaScreen(
                    navigateToDetails = { id ->
                        navController.navigate(route = AniListRoute.MEDIA_DETAIL_ROUTE + "/$id")
                    },
                    isAnime = true,
                )
            } else {
                PleaseLogin()
            }
        }
        composable(AniListRoute.MANGA_ROUTE) {
            setBottomBarState(true)
            if (accessCode != "") {
                MyMediaScreen(
                    navigateToDetails = { id ->
                        navController.navigate(route = AniListRoute.MEDIA_DETAIL_ROUTE + "/$id")
                    },
                    isAnime = false,
                )
            } else {
                PleaseLogin()
            }
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
