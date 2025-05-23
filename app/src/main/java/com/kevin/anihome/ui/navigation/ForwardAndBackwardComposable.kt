package com.kevin.anihome.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.composableMaterialForwardBackWard(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        // pop is back and not pop is forward
        enterTransition = {
            materialEnterTransition()
        },
        popEnterTransition = {
            materialPopEnterTransition()
        },
        exitTransition = {
            materialExitTransition()
        },
        popExitTransition = {
            materialPopExitTransition()
        },
        content = content,
    )
}
