package com.example.anilist.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AniListBottomNavigationBar(
    selectedDestination: String,
    navigateToTopLevelDestination: (AnilistTopLevelDestination) -> Unit
) {
    NavigationBar() {
        TOP_LEVEL_DESTINATIONS.forEach { destination ->
            NavigationBarItem(
                selected = selectedDestination == destination.route,
                onClick = { navigateToTopLevelDestination(destination) },
                icon = {
                    Icon(
                        imageVector = destination.selectedIcon,
                        contentDescription = stringResource(
                            id = destination.iconTextId
                        )
                    )
                },
                label = { Text(stringResource(id = destination.iconTextId))}
            )
        }
    }
}

@Preview()
@Composable
fun AniBottomNavigationBarPreview() {
    AniListBottomNavigationBar(selectedDestination = , navigateToTopLevelDestination = )
}
