package com.example.anilist.ui.forum

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ForumScreen() {
    Text(text = "Forum", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
}

@Preview(showBackground = true)
@Composable
fun ForumScreenPreview() {
    ForumScreen()
}