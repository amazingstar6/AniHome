package com.example.anilist

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.anilist.ui.home.AniHomeViewModel
import com.example.anilist.ui.navigation.AniListBottomNavigationBar
import com.example.anilist.ui.navigation.AniListNavigationActions
import com.example.anilist.ui.navigation.AniListRoute
import com.example.anilist.ui.navigation.AniNavGraph
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
//                    val mediaDetailsViewModel: MediaDetailsViewModel = hiltViewModel()

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
                        AniNavGraph(
                            navController = navController,
                            navigationActions = navigationAction,
                            modifier = Modifier.padding(bottom = it.calculateBottomPadding()),
                            aniHomeViewModel = aniHomeViewModel,
                            userSettings = userSettings
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


}