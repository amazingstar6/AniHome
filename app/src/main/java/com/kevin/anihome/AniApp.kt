package com.kevin.anihome

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kevin.anihome.ui.navigation.AniListBottomNavigationBar
import com.kevin.anihome.ui.navigation.AniListNavigationActions
import com.kevin.anihome.ui.navigation.AniListNavigationRail
import com.kevin.anihome.ui.navigation.AniListRoute
import com.kevin.anihome.ui.navigation.AniNavHost
import com.kevin.anihome.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.kevin.anihome.utils.Apollo
import com.kevin.anihome.utils.LoadingCircle

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AniApp(
    uiState: MainActivityUiState,
    windowSize: WindowSizeClass,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.consumeWindowInsets(WindowInsets(0.dp)),
    ) {
        val navController = rememberNavController()
        val navigationAction =
            remember(navController) {
                AniListNavigationActions(navController)
            }
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val selectedDestination = navBackStackEntry?.destination?.route ?: AniListRoute.HOME_ROUTE

        var showBottomBar by remember {
            mutableStateOf(true)
        }
        val bottomBarIsVisible =
            navController.currentBackStackEntryAsState().value?.destination?.route in
                TOP_LEVEL_DESTINATIONS.map {
                    it.route
                }

        Scaffold(bottomBar = {
            AnimatedVisibility(
                windowSize.widthSizeClass == WindowWidthSizeClass.Compact && bottomBarIsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                AniListBottomNavigationBar(
                    selectedDestination = selectedDestination,
                    navigateToTopLevelDestination = navigationAction::navigateTo,
                )
            }
        }) {
            when (uiState) {
                is MainActivityUiState.Loading -> {
                    LoadingCircle()
                }

                is MainActivityUiState.Success -> {
                    MainActivity.accessCode = uiState.userData.accessCode
                    Apollo.setAccessCode(MainActivity.accessCode)
                    Row {
                        if (windowSize.widthSizeClass == WindowWidthSizeClass.Medium || windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) {
                            AniListNavigationRail(
                                selectedDestination = selectedDestination,
                                navigateToTopLevelDestination = navigationAction::navigateTo,
                            )
                        }
                        AniNavHost(
                            accessCode = MainActivity.accessCode,
                            navController = navController,
                            navigationActions = navigationAction,
                            // fixme navigation bar has a height of 80.dp https://m3.material.io/components/navigation-bar/specs
                            modifier =
                                Modifier.padding(
                                    bottom = if (bottomBarIsVisible) 80.dp else 0.dp, // it.calculateBottomPadding()
                                ),
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
