package com.example.anilist

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
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
import com.example.anilist.data.models.AniMediaListSort
import com.example.anilist.data.repository.Theme
import com.example.anilist.utils.LoadingCircle
import com.example.anilist.ui.navigation.AniListBottomNavigationBar
import com.example.anilist.ui.navigation.AniListNavigationActions
import com.example.anilist.ui.navigation.AniListNavigationRail
import com.example.anilist.ui.navigation.AniListRoute
import com.example.anilist.ui.navigation.AniNavHost
import com.example.anilist.ui.theme.AnilistTheme
import com.example.anilist.utils.Apollo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        var accessCode = ""
        var userId = -1
        var mediaListSort = AniMediaListSort.UPDATED_TIME_DESC
    }


    private val viewModel: MainActivityViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree()) // fixme disable for release

        // processing the uri received for logging in
        if (intent?.data != null) {
            Timber.i("Data is " + intent.data)
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
            Timber.i("Access code is $accessCode, token type is $tokenType, expires in $expiresIn seconds")
            viewModel.saveAccessCodeAndUserId(accessCode, tokenType, expiresIn)
        }

        lifecycleScope.launch {
            viewModel
                .toastMessage
                .collect { message ->
                    Toast.makeText(
                        this@MainActivity,
                        message,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
        }

        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)
        // Update the uiState
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach {
                        uiState = it
                        if (it is MainActivityUiState.Success) {
                            userId = it.userData.userId
                            mediaListSort = it.userData.mediaListSort
                        }
                    }
                    .collect()
            }
        }

        setContent {
            AnilistTheme(
                darkTheme = when (uiState) {
                    is MainActivityUiState.Loading -> isSystemInDarkTheme()
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
                    val windowSize = calculateWindowSizeClass(this)
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
                        if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) {
                            AniListBottomNavigationBar(
                                selectedDestination = selectedDestination,
                                navigateToTopLevelDestination = navigationAction::navigateTo,
                                showBottomBar,
                            )
                        }

                    }) {
                        when (uiState) {
                            is MainActivityUiState.Loading -> {
                                LoadingCircle()
                            }

                            is MainActivityUiState.Success -> {
                                Timber.d("Access code when starting up is " + (uiState as MainActivityUiState.Success).userData.accessCode)
                                accessCode =
                                    (uiState as MainActivityUiState.Success).userData.accessCode
                                Apollo.setAccessCode(accessCode)
                                Row {
                                    if (windowSize.widthSizeClass == WindowWidthSizeClass.Medium
                                        || windowSize.widthSizeClass == WindowWidthSizeClass.Expanded
                                    ) {
                                        AniListNavigationRail(
                                            selectedDestination = selectedDestination,
                                            navigateToTopLevelDestination = navigationAction::navigateTo,
                                            showBottomBar,
                                        )
                                    }
                                    AniNavHost(
                                        accessCode = accessCode,
                                        navController = navController,
                                        navigationActions = navigationAction,
                                        modifier = Modifier.padding(bottom = it.calculateBottomPadding()),
                                        setBottomBarState = { newValue ->
                                            showBottomBar = newValue
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
