package com.example.anilist.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.anilist.ui.PleaseLogin
import com.example.anilist.ui.details.CoverLarge
import com.example.anilist.ui.details.activitydetail.ActivityDetailScreen
import com.example.anilist.ui.details.characterdetail.CharacterDetailScreen
import com.example.anilist.ui.details.forumdetail.ForumDetailScreen
import com.example.anilist.ui.details.mediadetails.MediaDetail
import com.example.anilist.ui.details.reviewdetail.ReviewDetailScreen
import com.example.anilist.ui.details.staffdetail.StaffDetailScreen
import com.example.anilist.ui.details.studiodetail.StudioDetailScreen
import com.example.anilist.ui.details.threadcommentdetail.ThreadCommentScreen
import com.example.anilist.ui.details.userdetail.UserDetailScreen
import com.example.anilist.ui.feed.FeedScreen
import com.example.anilist.ui.forum.ForumScreen
import com.example.anilist.ui.home.HomeScreen
import com.example.anilist.ui.home.HomeViewModel
import com.example.anilist.ui.home.MediaOverview
import com.example.anilist.ui.home.SettingsScreen
import com.example.anilist.ui.home.notifications.NotificationScreen
import com.example.anilist.ui.home.notifications.UnreadNotificationsViewModel
import com.example.anilist.ui.mymedia.MyMediaScreen
import timber.log.Timber

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
    val unreadNotificationsViewModel: UnreadNotificationsViewModel = hiltViewModel()
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = AniListRoute.HOME_ROUTE,
    ) {
        composable(
            AniListRoute.HOME_ROUTE,
//            popEnterTransition = { scaleIn() }
        ) {
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
                navigateToOverview = navigationActions::navigateToOverview,
                unreadNotificationsViewModel = unreadNotificationsViewModel
            )
        }
        composable(
            route = AniListRoute.MEDIA_OVERVIEW_ROUTE + "/{trendingType}",
            arguments = listOf(navArgument("trendingType") {
                type = NavType.IntType
            }),
            enterTransition = { slideIn(initialOffset = { IntOffset(x = it.width, y = 0) }) },
            exitTransition = { slideOut(targetOffset = { IntOffset(x = it.width, y = 0) }) }
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
            enterTransition = {
                slideIn(initialOffset = {
                    IntOffset(
                        x = it.width / 2,
                        y = 0
                    )
                }) + fadeIn()
            },
            exitTransition = {
                slideOut(targetOffset = {
                    IntOffset(
                        x = it.width / 2,
                        y = 0
                    )
                }) + fadeOut()
            },
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
            route = "${AniListRoute.CHARACTER_DETAIL_ROUTE}/{${AniListRoute.CHARACTER_DETAIL_KEY}}",
            arguments = listOf(navArgument(AniListRoute.CHARACTER_DETAIL_KEY) { type = NavType.IntType }),
            content = { backStackEntry ->
                CharacterDetailScreen(
                    id = backStackEntry.arguments?.getInt(AniListRoute.CHARACTER_DETAIL_KEY) ?: -1,
                    onNavigateToMedia = navigationActions::navigateToMediaDetails,
                    onNavigateToStaff = navigationActions::navigateToStaff,
                    onNavigateBack = navigationActions::navigateBack,
                )
            },
        )
        composable(
            route = AniListRoute.STAFF_DETAIL_ROUTE + "/{${AniListRoute.STAFF_DETAIL_ID_KEY}}",
            arguments = listOf(navArgument(AniListRoute.STAFF_DETAIL_ID_KEY) {
                type = NavType.IntType
            }),
            content = { backStackEntry ->
                StaffDetailScreen(
                    id = backStackEntry.arguments?.getInt(AniListRoute.STAFF_DETAIL_ID_KEY) ?: -1,
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
                onNavigateToUser = navigationActions::navigateToUser,
                onNavigateToThread = navigationActions::navigateToThread,
                onNavigateToThreadComment = navigationActions::navigateToThreadComment,
                unreadNotificationsViewModel = unreadNotificationsViewModel
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
            route = AniListRoute.THREAD_COMMENT + "/{commentId}",
            arguments = listOf(navArgument("commentId") { type = NavType.IntType }),
            content = { navBackStackEntry ->
                ThreadCommentScreen(
                    commentId = navBackStackEntry.arguments?.getInt("commentId") ?: -1,
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
            Timber.d("Access code is $accessCode")
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

