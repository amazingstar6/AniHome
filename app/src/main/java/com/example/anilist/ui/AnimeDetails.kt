package com.example.anilist.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AnimeDetails(id: Int) {
    // show this when clicking on an anime card
    Text("Showing anime with id $id")
}