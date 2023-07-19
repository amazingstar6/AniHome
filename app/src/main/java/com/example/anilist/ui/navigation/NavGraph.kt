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
import com.example.anilist.ui.home.AniHome
import com.example.anilist.ui.home.AniHomeViewModel
import com.example.anilist.ui.EmptyComingSoon
import com.example.anilist.ui.PleaseLogin
import com.example.anilist.ui.feed.FeedScreen
import com.example.anilist.ui.forum.ForumScreen
import com.example.anilist.ui.home.NotificationScreen
import com.example.anilist.ui.home.SettingsScreen
import com.example.anilist.ui.media_details.MediaDetail
import com.example.anilist.ui.my_media.MyAnime

private const val TAG = "AniNavGraph"

@Composable
fun AniNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    navigationActions: AniListNavigationActions,
    aniHomeViewModel: AniHomeViewModel,
    userSettings: UserSettings?,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = AniListRoute.HOME_ROUTE
    ) {
        composable(AniListRoute.HOME_ROUTE) {
            AniHome(
                aniHomeViewModel = aniHomeViewModel,
                onNavigateToNotification = { navController.navigate(route = AniListRoute.NOTIFICATION_ROUTE) },
                onNavigateToDetails = navigationActions::navigateToMediaDetails,
                onNavigateToSettings = {
                    navController.navigate(route = AniListRoute.SETTINGS)
                })
        }
        composable(
            AniListRoute.ANIME_DETAIL_ROUTE + "/{animeId}",
            arguments = listOf(navArgument("animeId") {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            MediaDetail(
                mediaId = backStackEntry.arguments?.getInt("animeId") ?: -1,
                onNavigateToDetails = navigationActions::navigateToMediaDetails,
                onNavigateBack = {
                    navController.popBackStack()
                })
        }
        composable(
            AniListRoute.NOTIFICATION_ROUTE,
        ) {
            NotificationScreen()
        }
        composable(
            AniListRoute.SETTINGS
        ) {
            SettingsScreen()
        }
        composable(AniListRoute.ANIME_ROUTE) {
//            val trendingAnimeUiState by aniHomeViewModel.uiState.collectAsState()
            Log.i(TAG, "Access code in #3 is ${userSettings?.accessCode}")
            if (userSettings?.accessCode != "" && userSettings == null) {
                Log.i(TAG, "ACCESS CODE STORED IS ${userSettings?.accessCode}")
                MyAnime(
                    aniHomeViewModel,
                    { id ->
                        navController.navigate(route = AniListRoute.ANIME_DETAIL_ROUTE + "/$id")
//                        toggleNavBarOff()
                    },
                    true,
//                        accessCode = userSettings.accessCode ?: ""
                    accessCode = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjI0MzFmYjM2MjMwNWFiM2Y2NzI5MmNlMGY4M2RmOGM0MmM1NGI0NmEyYmI5NjNlN2ZiODA1YzI1YTU0ZGU4ZjJmZTk5NWUxMDEwNGRiNWUxIn0.eyJhdWQiOiIxMzYxNiIsImp0aSI6IjI0MzFmYjM2MjMwNWFiM2Y2NzI5MmNlMGY4M2RmOGM0MmM1NGI0NmEyYmI5NjNlN2ZiODA1YzI1YTU0ZGU4ZjJmZTk5NWUxMDEwNGRiNWUxIiwiaWF0IjoxNjg5NzYwNTQ3LCJuYmYiOjE2ODk3NjA1NDcsImV4cCI6MTcyMTM4Mjk0Nywic3ViIjoiODA4NjI2Iiwic2NvcGVzIjpbXX0.RD0LdCD8AmzNlJM_OIWbrPz-Ec9RBKZrtGE0vWhvM7G9cs5vWf4WF3QGrd0P5k5-YZJB_Cr7YJCe8mV_n2B0yHm3Ia0kde7gdRx1V9aaXDRNH-MNidYjVq-RuVLfkI-bgw82vGXQ42Y_dhFZypJiYdh2SYIY09OWgNqwvxLu-D-EYVJMBEdsWbd6RRJdKzyCQ0EMsUxgmBCgHuMt2KghA5FMhTj_eWzT30rEs1ziREGTpIz_aJS-pHed8husWF-WhwC0YY0r0NXbuge--tpGvGd8ShJb2AQ0lDvQ7JomvFlkqEUXZf7jC_rQqeLApKqnx-iwCTy-0JMDLuRGHkvXtn6pGCgdFwySTLfoMYInXX3vuYMxlfuheINkkx2qL5n51PlMRXRaADfH_C2jmFFU5fNLHFSmTE_tvVMMdflEmQWwt1htz1g-EZp9wGMC88j56fXpoNeI4htppkOWn5mLioyZFoX5hqD7zPu3yQd6A9cistkQWvz0VBIOGQH0d-5eKlVkHIQpP289cx3ho2abxBmilDQsF0RhOueLaSPHtsuyuvOie-gcvmQUPYuMEkwUpEvyxrXqg976YwTGCcN1Rl6BG4RhCgj5ngF3HhPTj4HSO1X5-gynKLcL3S61yjD6je3WecFrYqj8xStZekffuYcD0TKyAOYE0BQndC7xQTg"
                )
            } else {
                PleaseLogin()
            }
        }
        composable(AniListRoute.MANGA_ROUTE) {
            MyAnime(
                aniHomeViewModel,
                navigateToDetails = { id ->
                    navController.navigate(route = AniListRoute.ANIME_DETAIL_ROUTE + "/$id")
                },
                isAnime = false,
                accessCode = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjI0MzFmYjM2MjMwNWFiM2Y2NzI5MmNlMGY4M2RmOGM0MmM1NGI0NmEyYmI5NjNlN2ZiODA1YzI1YTU0ZGU4ZjJmZTk5NWUxMDEwNGRiNWUxIn0.eyJhdWQiOiIxMzYxNiIsImp0aSI6IjI0MzFmYjM2MjMwNWFiM2Y2NzI5MmNlMGY4M2RmOGM0MmM1NGI0NmEyYmI5NjNlN2ZiODA1YzI1YTU0ZGU4ZjJmZTk5NWUxMDEwNGRiNWUxIiwiaWF0IjoxNjg5NzYwNTQ3LCJuYmYiOjE2ODk3NjA1NDcsImV4cCI6MTcyMTM4Mjk0Nywic3ViIjoiODA4NjI2Iiwic2NvcGVzIjpbXX0.RD0LdCD8AmzNlJM_OIWbrPz-Ec9RBKZrtGE0vWhvM7G9cs5vWf4WF3QGrd0P5k5-YZJB_Cr7YJCe8mV_n2B0yHm3Ia0kde7gdRx1V9aaXDRNH-MNidYjVq-RuVLfkI-bgw82vGXQ42Y_dhFZypJiYdh2SYIY09OWgNqwvxLu-D-EYVJMBEdsWbd6RRJdKzyCQ0EMsUxgmBCgHuMt2KghA5FMhTj_eWzT30rEs1ziREGTpIz_aJS-pHed8husWF-WhwC0YY0r0NXbuge--tpGvGd8ShJb2AQ0lDvQ7JomvFlkqEUXZf7jC_rQqeLApKqnx-iwCTy-0JMDLuRGHkvXtn6pGCgdFwySTLfoMYInXX3vuYMxlfuheINkkx2qL5n51PlMRXRaADfH_C2jmFFU5fNLHFSmTE_tvVMMdflEmQWwt1htz1g-EZp9wGMC88j56fXpoNeI4htppkOWn5mLioyZFoX5hqD7zPu3yQd6A9cistkQWvz0VBIOGQH0d-5eKlVkHIQpP289cx3ho2abxBmilDQsF0RhOueLaSPHtsuyuvOie-gcvmQUPYuMEkwUpEvyxrXqg976YwTGCcN1Rl6BG4RhCgj5ngF3HhPTj4HSO1X5-gynKLcL3S61yjD6je3WecFrYqj8xStZekffuYcD0TKyAOYE0BQndC7xQTg"
            )
        }
        composable(AniListRoute.FEED_ROUTE) {
            FeedScreen()
        }
        composable(AniListRoute.FORUM_ROUTE) {
            ForumScreen()
        }
    }
}