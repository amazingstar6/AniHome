package com.example.anilist.ui.mymedia.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.anilist.R

@Composable
fun NoMediaScreen(isAnime: Boolean) {
    Box(contentAlignment = Alignment.Center) {
        Text(
            text =
                if (isAnime) {
                    stringResource(R.string.no_anime_add_something_to_your_list)
                } else {
                    stringResource(
                        R.string.no_manga_add_something_to_your_list,
                    )
                },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NoMediaPreview() {
    NoMediaScreen(true)
}
