package com.example.anilist.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AnimeDetails(
    id: Int,
    navigateToHome: () -> Unit
) {
    // show this when clicking on an anime card
    Column() {
        IconButton(onClick = navigateToHome) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = Icons.Default.ArrowBack.toString()
            )
        }
        Text("Showing anime with id $id", modifier = Modifier.padding(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun AnimeDetailsPreview() {
    AnimeDetails(
        id = 150672,
        navigateToHome = { }
    )
}