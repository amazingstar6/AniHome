package com.kevin.anihome.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.kevin.anihome.R
import com.kevin.anihome.data.models.HomeTrendingTypes
import com.kevin.anihome.ui.Dimens
import com.kevin.anihome.utils.MEDIUM_MEDIA_WIDTH

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MediaOverview(
    ordinalNumber: Int,
    navigateBack: () -> Unit,
    navigateToDetails: (Int) -> Unit,
    homeViewModel: HomeViewModel,
) {
    val type = HomeTrendingTypes.entries[ordinalNumber]
    val pager =
        when (type) {
            HomeTrendingTypes.TRENDING_NOW -> homeViewModel.trendingNowPager
            HomeTrendingTypes.POPULAR_THIS_SEASON -> homeViewModel.popularThisSeasonPager
            HomeTrendingTypes.UPCOMING_NEXT_SEASON -> homeViewModel.upComingNextSeasonPager
            HomeTrendingTypes.ALL_TIME_POPULAR -> homeViewModel.allTimePopularPager
            HomeTrendingTypes.TOP_100_ANIME -> homeViewModel.top100AnimePager
            HomeTrendingTypes.POPULAR_MANHWA -> homeViewModel.popularManhwaPager
        }.collectAsLazyPagingItems()

    Scaffold(topBar = {
        TopAppBar(title = { Text(text = type.toString(LocalContext.current)) }, navigationIcon = {
            IconButton(
                onClick = navigateBack,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back),
                )
            }
        })
    }) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(MEDIUM_MEDIA_WIDTH.dp),
            contentPadding = PaddingValues(horizontal = Dimens.PaddingSmall),
            modifier = Modifier.padding(top = it.calculateTopPadding()),
        ) {
            items(pager.itemCount) { index ->
                val media = pager[index]
                AnimeCard(
                    title = media?.title ?: "",
                    coverImage = media?.coverImage ?: "",
                    onNavigateToDetails = { navigateToDetails(media?.id ?: -1) },
                )
            }
        }
    }
}
