package com.example.anilist.ui.details.activitydetail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ActivityDetailScreen(activityId: Int, navigateBack: () -> Unit) {
    Text(text = "Showing activity with id $activityId")
}