package com.example.anilist.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anilist.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotificationScreen(aniHomeViewModel: AniHomeViewModel = viewModel()) {
    val filterList = listOf<String>("All", "Airing", "Activity", "Forum", "Follows", "Media")
    var currentIndex by remember {
        mutableStateOf(0)
    }
    Column() {
        FlowRow() {
            filterList.forEachIndexed { index, filter ->
                FilterChip(
                    selected = index == currentIndex,
                    onClick = { currentIndex = index },
                    label = { Text(text = filter) })
            }
        }
        OutlinedButton(onClick = { aniHomeViewModel.markAllNotificationsAsRead() } ) {
            Text(text = stringResource(R.string.mark_all_as_read))
        }
        LazyColumn() {
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    NotificationScreen()
}