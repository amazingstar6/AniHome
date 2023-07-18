package com.example.anilist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.anilist.ui.AniHome
import com.example.anilist.ui.AniHomeViewModel
import com.example.anilist.ui.animeDetails.AnimeDetails
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
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navigationAction = remember(navController) {
                        AniListNavigationActions(navController)
                    }
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val selectedDestination =
                        navBackStackEntry?.destination?.route ?: AniListRoute.HOME_ROUTE

                    var visible by remember {
                        mutableStateOf(true)
                    }
                    Scaffold(bottomBar = {
                        AniListBottomNavigationBar(
                            selectedDestination = selectedDestination,
                            navigateToTopLevelDestination = navigationAction::navigateTo,
                            visible
                        )
                    }) {
                        AniListNavHost(
                            navController = navController,
                            modifier = Modifier.padding(bottom = it.calculateBottomPadding()),
                            toggleNavBar = { visible = true },
                            toggleNavBarOff = { visible = false }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun AniListNavHost(
        navController: NavHostController,
        modifier: Modifier = Modifier,
        toggleNavBar: () -> Unit,
        toggleNavBarOff: () -> Unit
    ) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = AniListRoute.HOME_ROUTE
        ) {
            composable(AniListRoute.HOME_ROUTE) {
                val aniHomeViewModel: AniHomeViewModel by viewModels()
                AniHome(aniHomeViewModel = aniHomeViewModel) { id ->
                    navController.navigate(
                        route = AniListRoute.ANIME_DETAIL_ROUTE + "/$id"
                    )
                    toggleNavBarOff()
                }
            }
            composable(
                AniListRoute.ANIME_DETAIL_ROUTE + "/{animeId}",
                arguments = listOf(navArgument("animeId") {
                    type = NavType.IntType
                })
            ) { backStackEntry ->
                AnimeDetails(
                    backStackEntry.arguments?.getInt("animeId") ?: -1,
                    onNavigateToDetails = { id ->
                        navController.navigate(
                            route = AniListRoute.ANIME_DETAIL_ROUTE + "/$id"
                        )
                        toggleNavBarOff()
                    },
//                    anime = Anime(),
                    navigateBack = {
                        toggleNavBar()
                        onBackPressedDispatcher.onBackPressed()
                    })
            }
            composable(AniListRoute.ANIME_ROUTE) {
                MyAnime()
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