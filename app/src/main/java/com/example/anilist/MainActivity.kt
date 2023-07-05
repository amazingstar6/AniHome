package com.example.anilist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.anilist.ui.AniHome
import com.example.anilist.ui.AnimeDetails
import com.example.anilist.ui.EmptyComingSoon
import com.example.anilist.ui.navigation.AniListBottomNavigationBar
import com.example.anilist.ui.navigation.AniListNavigationActions
import com.example.anilist.ui.navigation.AniListRoute
import com.example.anilist.ui.theme.AnilistTheme

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnilistTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navigationAction = remember(navController) {
                        AniListNavigationActions(navController)
                    }
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val selectedDestination = navBackStackEntry?.destination?.route ?: AniListRoute.HOME_ROUTE

                    Column(modifier = Modifier.fillMaxSize()) {
                        AniListNavHost(navController = navController, modifier = Modifier.weight(1f))

                        AniListBottomNavigationBar(
                            selectedDestination = selectedDestination,
                            navigateToTopLevelDestination = navigationAction::navigateTo
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun AniListNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = AniListRoute.HOME_ROUTE
        ) {
            composable(AniListRoute.HOME_ROUTE) {
                AniHome {id ->
                    navController.navigate(
                        route = AniListRoute.ANIME_DETAIL_ROUTE + "/$id",
                    )
                }
            }
            composable(
                AniListRoute.ANIME_DETAIL_ROUTE + "/{animeId}",
                arguments = listOf(navArgument("animeId") {
                    type = NavType.IntType
                })
            ) { backStackEntry ->
                AnimeDetails(backStackEntry.arguments?.getInt("animeId") ?: -1) {
                    navController.navigate(route = AniListRoute.HOME_ROUTE)
                }
            }
            composable(AniListRoute.ANIME_ROUTE) {
                EmptyComingSoon()
            }
            composable(AniListRoute.MANGA_ROUTE) {
                EmptyComingSoon()
            }
            composable(AniListRoute.FEED_ROUTE) {
                EmptyComingSoon()
            }
            composable(AniListRoute.FORUM_ROUTE) {
                EmptyComingSoon()
            }
        }
    }
}