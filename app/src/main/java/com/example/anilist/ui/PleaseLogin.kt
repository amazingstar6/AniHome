package com.example.anilist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PleaseLogin() {
    val uriHandler = LocalUriHandler.current
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Please login to use this feature",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Button(onClick = {
//            uriHandler.openUri("https://anilist.co/api/v2/oauth/authorize?client_id=13616&redirect_uri=anihome://login&response_type=code")
            uriHandler.openUri("https://anilist.co/api/v2/oauth/authorize?client_id=13616&response_type=token")
        }) {
            Text("Login to AniList")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PleaseLoginPreview() {
    PleaseLogin()
}
