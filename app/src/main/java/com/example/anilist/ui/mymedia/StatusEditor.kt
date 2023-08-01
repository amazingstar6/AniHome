package com.example.anilist.ui.mymedia

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun StatusEditor(listEntryId: Int) {
    Text(text = "Editing status of media with id $listEntryId")
}