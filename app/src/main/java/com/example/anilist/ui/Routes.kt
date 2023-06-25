package com.example.anilist.ui

sealed class Routes(val route: String) {
    object Trending : Routes("trending")
    object AnimeDetails: Routes("AnimeDetails")

    companion object {
        /**
         * Returns path for AnimeDetails with the given argument.
         * @param argument the id of the anime show
         */
        fun addRouteToAnimeDetails(argument: Int): String {
            return "AnimeDetails/${argument}"
        }
    }
}

fun main() {
    print(Routes.addRouteToAnimeDetails(123012))
}
