package com.example.anilist

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.anilist.ui.AnimeDetails
import com.example.anilist.ui.Routes
import com.example.anilist.ui.theme.AnilistTheme
import com.example.anilist.ui.TrendingAnime

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
                    NavHost(
                        navController = navController,
                        startDestination = Routes.Trending.route
                    ) {
                        composable(
                            Routes.Trending.route
                        ) {
                            TrendingAnime {
                                navController.navigate(
                                    route = Routes.addRouteToAnimeDetails(it)
                                )
                            }
                        }
                        composable(Routes.AnimeDetails.route + "/{animeId}",
                            arguments = listOf(navArgument("animeId") {
                                type = NavType.IntType
                            })
                        ) { backStackEntry ->
                            AnimeDetails(backStackEntry.arguments?.getInt("animeId") ?: -1)
                        }
                    }
                }
            }
        }
    }

    @Preview(showBackground = true, widthDp = 320, heightDp = 320)
    @Composable
    fun GreetingPreview() {
        AnilistTheme {
            TrendingAnime {  }
        }
    }
}