package com.kevin.anihome.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

/**
 * Shows a navigation bar for the top level destinations in the app
 */
@Composable
fun AniListBottomNavigationBar(
    selectedDestination: String,
    navigateToTopLevelDestination: (AnilistTopLevelDestination) -> Unit,
) {
    NavigationBar {
        TOP_LEVEL_DESTINATIONS.forEach { destination ->
            NavigationBarItem(
                selected = selectedDestination == destination.route,
                onClick = { navigateToTopLevelDestination(destination) },
                icon = {
                    if (selectedDestination == destination.route) {
                        Icon(
                            painter = painterResource(id = destination.selectedIcon),
                            contentDescription =
                                stringResource(
                                    id = destination.iconTextId,
                                ),
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = destination.unselectedIcon),
                            contentDescription =
                                stringResource(
                                    id = destination.iconTextId,
                                ),
                        )
                    }
                },
                label = { Text(stringResource(id = destination.iconTextId)) },
            )
        }
    }
}

/**
 * Shows a navigation rail for the top level destinations in the app
 */
@Composable
fun AniListNavigationRail(
    selectedDestination: String,
    navigateToTopLevelDestination: (AnilistTopLevelDestination) -> Unit,
) {
    NavigationRail(modifier = Modifier.fillMaxHeight()) {
        Spacer(modifier = Modifier.weight(1f))
        TOP_LEVEL_DESTINATIONS.forEach { destination ->
            NavigationRailItem(
                selected = selectedDestination == destination.route,
                onClick = { navigateToTopLevelDestination(destination) },
                icon = {
                    if (selectedDestination == destination.route) {
                        Icon(
                            painter = painterResource(id = destination.selectedIcon),
                            contentDescription =
                                stringResource(
                                    id = destination.iconTextId,
                                ),
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = destination.unselectedIcon),
                            contentDescription =
                                stringResource(
                                    id = destination.iconTextId,
                                ),
                        )
                    }
                },
                label = { Text(stringResource(id = destination.iconTextId)) },
                alwaysShowLabel = false,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview
@Composable
fun AniBottomNavigationBarPreview() {
    AniListBottomNavigationBar(
        selectedDestination = AniListRoute.HOME_ROUTE,
        navigateToTopLevelDestination = { },
    )
}

@Preview
@Composable
fun AniNavigationRailPreview() {
    AniListNavigationRail(
        selectedDestination = AniListRoute.HOME_ROUTE,
        navigateToTopLevelDestination = { },
    )
}
