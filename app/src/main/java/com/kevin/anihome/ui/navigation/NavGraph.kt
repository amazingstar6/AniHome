package com.kevin.anihome.ui.navigation

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
import com.kevin.anihome.ui.PleaseLogin
import com.kevin.anihome.ui.details.CoverLarge
import com.kevin.anihome.ui.details.activitydetail.ActivityDetailScreen
import com.kevin.anihome.ui.details.characterdetail.CharacterDetailScreen
import com.kevin.anihome.ui.details.forumdetail.ForumDetailScreen
import com.kevin.anihome.ui.details.mediadetails.MediaDetail
import com.kevin.anihome.ui.details.reviewdetail.ReviewDetailScreen
import com.kevin.anihome.ui.details.staffdetail.StaffDetailScreen
import com.kevin.anihome.ui.details.studiodetail.StudioDetailScreen
import com.kevin.anihome.ui.details.threadcommentdetail.ThreadCommentScreen
import com.kevin.anihome.ui.details.userdetail.UserDetailScreen
import com.kevin.anihome.ui.feed.FeedScreen
import com.kevin.anihome.ui.forum.ForumScreen
import com.kevin.anihome.ui.home.HomeScreen
import com.kevin.anihome.ui.home.HomeViewModel
import com.kevin.anihome.ui.home.MediaOverview
import com.kevin.anihome.ui.home.SettingsScreen
import com.kevin.anihome.ui.home.notifications.NotificationScreen
import com.kevin.anihome.ui.home.notifications.UnreadNotificationsViewModel
import com.kevin.anihome.ui.mymedia.MyMediaScreen
import com.kevin.anihome.utils.materialEnterTransitionSpec
import com.kevin.anihome.utils.materialExitTransitionSpec
import timber.log.Timber

/**
 * Contains the screens for the top level destinations above the navigation bar
 */
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
                unreadNotificationsViewModel = unreadNotificationsViewModel,
            )
        }
        composableMaterialForwardBackWard(
            route = AniListRoute.MEDIA_OVERVIEW_ROUTE + "/{trendingType}",
            arguments =
                listOf(
                    navArgument("trendingType") {
                        type = NavType.IntType
                    },
                ),
        ) { navBackStackEntry ->
            val ordinalNumber = navBackStackEntry.arguments?.getInt("trendingType") ?: -1
            setBottomBarState(false)
            MediaOverview(
                homeViewModel = homeViewModel,
                ordinalNumber = ordinalNumber,
                navigateBack = navigationActions::navigateBack,
                navigateToDetails = navigationActions::navigateToMediaDetails,
            )
        }
        composableMaterialForwardBackWard(
            "${AniListRoute.MEDIA_DETAIL_ROUTE}/{${AniListRoute.MEDIA_DETAIL_ID_KEY}}",
//            enterTransition = {
//                slideIn(initialOffset = {
//                    IntOffset(
//                        x = it.width / 4,
//                        y = 0
//                    )
//                }) + fadeIn()
//            },
//            popEnterTransition = {
//                slideIn(initialOffset = {
//                    IntOffset(
//                        x = -it.width / 4,
//                        y = 0
//                    )
//                }) + fadeIn()
//            },
//            exitTransition = {
//                slideOut(targetOffset = {
//                    IntOffset(
//                        x = -it.width / 4,
//                        y = 0
//                    )
//                }) + fadeOut()
// //                scaleOut()
//            },
//            popExitTransition = {
//                slideOut(targetOffset = {
//                    IntOffset(
//                        x = it.width / 4,
//                        y = 0
//                    )
//                }) + fadeOut()
// //                scaleOut()
//            },
            arguments =
                listOf(
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
                navigateToStudioDetails = navigationActions::navigateToStudio,
            )
        }
        composableMaterialForwardBackWard(
            route = "${AniListRoute.CHARACTER_DETAIL_ROUTE}/{${AniListRoute.CHARACTER_DETAIL_KEY}}",
            arguments =
                listOf(
                    navArgument(AniListRoute.CHARACTER_DETAIL_KEY) {
                        type = NavType.IntType
                    },
                ),
            content = { backStackEntry ->
                CharacterDetailScreen(
                    id = backStackEntry.arguments?.getInt(AniListRoute.CHARACTER_DETAIL_KEY) ?: -1,
                    onNavigateToMedia = navigationActions::navigateToMediaDetails,
                    onNavigateToStaff = navigationActions::navigateToStaff,
                    onNavigateBack = navigationActions::navigateBack,
                )
            },
        )
        composableMaterialForwardBackWard(
            route = AniListRoute.STAFF_DETAIL_ROUTE + "/{${AniListRoute.STAFF_DETAIL_ID_KEY}}",
            arguments =
                listOf(
                    navArgument(AniListRoute.STAFF_DETAIL_ID_KEY) {
                        type = NavType.IntType
                    },
                ),
            content = { backStackEntry ->
                StaffDetailScreen(
                    id = backStackEntry.arguments?.getInt(AniListRoute.STAFF_DETAIL_ID_KEY) ?: -1,
                    onNavigateToCharacter = navigationActions::navigateToCharacter,
                    onNavigateToMedia = navigationActions::navigateToMediaDetails,
                    onNavigateBack = navigationActions::navigateBack,
                )
            },
        )
        composableMaterialForwardBackWard(
            route = AniListRoute.REVIEW_DETAIL_ROUTE + "/{reviewId}",
            arguments = listOf(navArgument("reviewId") { type = NavType.IntType }),
            content = { backStackEntry ->
                ReviewDetailScreen(
                    reviewId = backStackEntry.arguments?.getInt("reviewId") ?: -1,
                    onNavigateBack = navigationActions::navigateBack,
                )
            },
        )
        composableMaterialForwardBackWard(
            route = AniListRoute.STUDIO_DETAIL_ROUTE + "/{studioId}",
            arguments = listOf(navArgument("studioId") { type = NavType.IntType }),
            content = { backStackEntry ->
                StudioDetailScreen(
                    id = backStackEntry.arguments?.getInt("studioId") ?: -1,
                    navigateToMedia = navigationActions::navigateToMediaDetails,
                    navigateBack = navigationActions::navigateBack,
                )
            },
        )
        composableMaterialForwardBackWard(
            route = AniListRoute.THREAD_DETAIL_ROUTE + "/{forumId}",
            arguments = listOf(navArgument("forumId") { type = NavType.IntType }),
            content = { backStackEntry ->
                ForumDetailScreen(
                    id = backStackEntry.arguments?.getInt("forumId") ?: -1,
                )
            },
        )
        composableMaterialForwardBackWard(
            route = AniListRoute.USER_DETAIL_ROUTE + "/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType }),
            content = { backStackEntry ->
                UserDetailScreen(
                    id = backStackEntry.arguments?.getInt("userId") ?: -1,
                )
            },
        )
        composableMaterialForwardBackWard(
            route = AniListRoute.COVER_LARGE + "/{imageString}",
            arguments = listOf(navArgument("imageString") { type = NavType.StringType }),
            content = { navBackStackEntry ->
                CoverLarge(
                    coverImage = navBackStackEntry.arguments?.getString("imageString") ?: "",
                    navigateBack = navigationActions::navigateBack,
                )
            },
        )
        composableMaterialForwardBackWard(
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
                unreadNotificationsViewModel = unreadNotificationsViewModel,
            )
        }
        composableMaterialForwardBackWard(
            route = AniListRoute.ACTIVITY + "/{activityId}",
            arguments = listOf(navArgument("activityId") { type = NavType.IntType }),
            content = { navBackStackEntry ->
                ActivityDetailScreen(
                    activityId = navBackStackEntry.arguments?.getInt("activityId") ?: -1,
                    navigateBack = navigationActions::navigateBack,
                )
            },
        )
        composableMaterialForwardBackWard(
            route = AniListRoute.THREAD_COMMENT + "/{commentId}",
            arguments = listOf(navArgument("commentId") { type = NavType.IntType }),
            content = { navBackStackEntry ->
                ThreadCommentScreen(
                    commentId = navBackStackEntry.arguments?.getInt("commentId") ?: -1,
                    navigateBack = navigationActions::navigateBack,
                )
            },
        )
        composableMaterialForwardBackWard(
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

fun materialPopExitTransition() =
    slideOut(animationSpec = materialExitTransitionSpec(), targetOffset = {
        IntOffset(
            x = it.width / 4,
            y = 0,
        )
    }) + fadeOut(animationSpec = materialExitTransitionSpec())

fun materialExitTransition() =
    slideOut(animationSpec = materialExitTransitionSpec(), targetOffset = {
        IntOffset(
            x = -it.width / 4,
            y = 0,
        )
    }) + fadeOut(animationSpec = materialExitTransitionSpec())

fun materialPopEnterTransition() =
    slideIn(animationSpec = materialExitTransitionSpec(), initialOffset = {
        IntOffset(
            x = -it.width / 4,
            y = 0,
        )
    }) + fadeIn(animationSpec = materialExitTransitionSpec())

fun materialEnterTransition() =
    slideIn(
        animationSpec = materialEnterTransitionSpec(), initialOffset = {
            IntOffset(
                x = it.width / 4,
                y = 0,
            )
        },
    ) + fadeIn(animationSpec = materialEnterTransitionSpec())
