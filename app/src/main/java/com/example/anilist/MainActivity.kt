package com.example.anilist

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.anilist.data.repository.UserSettings
import com.example.anilist.ui.AniHome
import com.example.anilist.ui.AniHomeViewModel
import com.example.anilist.ui.EmptyComingSoon
import com.example.anilist.ui.MyAnime
import com.example.anilist.ui.PleaseLogin
import com.example.anilist.ui.animeDetails.AnimeDetails
import com.example.anilist.ui.navigation.AniListBottomNavigationBar
import com.example.anilist.ui.navigation.AniListNavigationActions
import com.example.anilist.ui.navigation.AniListRoute
import com.example.anilist.ui.theme.AnilistTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


private const val TAG = "MainActivity"

// for data store
// At the top level of your kotlin file:
//private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "USER_SETTINGS")

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var accessCode = "Not initialized"

    // requires api is cause by convert to RFC3339 function
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // processing the uri received for logging in
        if (intent?.data != null) {
            Log.i(TAG, "Data is ${intent.data.toString()}")
//            val code: String = intent.data.toString().substringAfter("anihome:://login?code=")
            val uri = intent.data.toString()
            val query = uri.substringAfter("#")
            val parameters = query.split("&")
            val parameterMap = parameters.associate {
                val (key, value) = it.split("=")
                key to value
            }
            accessCode = parameterMap["access_token"] ?: ""
            val tokenType = parameterMap["token_type"]
            val expiresIn = parameterMap["expires_in"]
            Log.i(
                TAG,
                "Access code is $accessCode, token type is $tokenType, expires in $expiresIn seconds"
            )

        }

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

                    val aniHomeViewModel: AniHomeViewModel = hiltViewModel()

                    aniHomeViewModel.initialSetupEvent.observe(this) { initialSetupEvent ->
                        observePreferenceChanges()
                    }


                    val userSettings by aniHomeViewModel.userSettings.observeAsState()
                    aniHomeViewModel.setAccessCode(
                        accessCode
                    )

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
                            toggleNavBarOff = { visible = false },
                            aniHomeViewModel,
                            userSettings
                        )
                    }
                }
            }
        }
    }

    private fun observePreferenceChanges() {
//        aniHomeViewModel.userSettings.observe(this) { userSettings ->
////            updateTaskFilters(userSettings.sortOrder, tasksUiModel.showCompleted)
//            accessCode = userSettings.accessCode
//        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertToRFC3339(secondsToAdd: Long): String {
        val currentTime = ZonedDateTime.now().plusSeconds(secondsToAdd)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
        return currentTime.format(formatter)
    }

    @Composable
    private fun AniListNavHost(
        navController: NavHostController,
        modifier: Modifier = Modifier,
        toggleNavBar: () -> Unit,
        toggleNavBarOff: () -> Unit,
        aniHomeViewModel: AniHomeViewModel,
        userSettings: UserSettings?
    ) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = AniListRoute.HOME_ROUTE
        ) {
            composable(AniListRoute.HOME_ROUTE) {
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
                    aniHomeViewModel = aniHomeViewModel,
                    navigateBack = {
                        toggleNavBar()
                        onBackPressedDispatcher.onBackPressed()
                    })
            }
            composable(AniListRoute.ANIME_ROUTE) {
                val trendingAnimeUiState by aniHomeViewModel.uiState.collectAsState()
                Log.i(TAG, "Access code in #3 is ${userSettings?.accessCode}")
                if (userSettings?.accessCode != "" && userSettings == null) {
                    Log.i(TAG, "ACCESS CODE STORED IS ${userSettings?.accessCode}")
                    MyAnime(
                        aniHomeViewModel,
                        { id ->
                            navController.navigate(route = AniListRoute.ANIME_DETAIL_ROUTE + "/$id")
                            toggleNavBarOff()
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