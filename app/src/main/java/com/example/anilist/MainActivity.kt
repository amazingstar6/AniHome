package com.example.anilist

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.anilist.data.repository.Theme
import com.example.anilist.ui.mediadetails.LoadingCircle
import com.example.anilist.ui.navigation.AniListBottomNavigationBar
import com.example.anilist.ui.navigation.AniListNavigationActions
import com.example.anilist.ui.navigation.AniListRoute
import com.example.anilist.ui.navigation.AniNavHost
import com.example.anilist.ui.theme.AnilistTheme
import com.example.anilist.utils.Apollo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        var accessCode = ""
    }


    private val viewModel: MainActivityViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // processing the uri received for logging in
        if (intent?.data != null) {
            Log.i(TAG, "Data is ${intent.data}")
//            val code: String = intent.data.toString().substringAfter("anihome:://login?code=")
            val uri = intent.data.toString()
            val query = uri.substringAfter("#")
            val parameters = query.split("&")
            val parameterMap = parameters.associate {
                val (key, value) = it.split("=")
                key to value
            }
            accessCode = parameterMap["access_token"] ?: ""
            val tokenType = parameterMap["token_type"] ?: ""
            val expiresIn = parameterMap["expires_in"] ?: ""
            Log.i(
                TAG,
                "Access code is $accessCode, token type is $tokenType, expires in $expiresIn seconds",
            )
            viewModel.saveAccessCode(accessCode, tokenType, expiresIn)
        }

        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)
        // Update the uiState
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach {
                        uiState = it
                    }
                    .collect()
            }
        }

        setContent {
            AnilistTheme(
                darkTheme = when (uiState) {
                    is MainActivityUiState.Loading ->  isSystemInDarkTheme()
                    is MainActivityUiState.Success -> {
                        when ((uiState as MainActivityUiState.Success).userData.theme) {
                            Theme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
                            Theme.LIGHT -> false
                            Theme.DARK -> true
                        }
                    }
                }
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    val navigationAction = remember(navController) {
                        AniListNavigationActions(navController)
                    }
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val selectedDestination =
                        navBackStackEntry?.destination?.route ?: AniListRoute.HOME_ROUTE

                    var showBottomBar by remember {
                        mutableStateOf(true)
                    }
                    Scaffold(bottomBar = {
                        AniListBottomNavigationBar(
                            selectedDestination = selectedDestination,
                            navigateToTopLevelDestination = navigationAction::navigateTo,
                            showBottomBar,
                        )
                    }) {
                        when (uiState) {
                            is MainActivityUiState.Loading -> {
                                LoadingCircle()
                            }
                            is MainActivityUiState.Success -> {
                                Log.d(TAG, "Access code when starting up is ${(uiState as MainActivityUiState.Success).userData.accessCode}")
                                accessCode = (uiState as MainActivityUiState.Success).userData.accessCode
                                Apollo.setAccessCode(accessCode)
                                AniNavHost(
                                    accessCode = accessCode,
                                    navController = navController,
                                    navigationActions = navigationAction,
                                    modifier = Modifier.padding(bottom = it.calculateBottomPadding()),
                                    setBottomBarState = { newValue -> showBottomBar = newValue },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
