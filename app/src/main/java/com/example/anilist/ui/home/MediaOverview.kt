package com.example.anilist.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.anilist.R
import com.example.anilist.data.models.HomeTrendingTypes
import com.example.anilist.ui.Dimens
import com.example.anilist.utils.AsyncImageRoundedCorners
import com.example.anilist.utils.shimmerBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaOverview(
    ordinalNumber: Int,
    navigateBack: () -> Unit,
    navigateToDetails: (Int) -> Unit,
    homeViewModel: HomeViewModel
) {
    val type = HomeTrendingTypes.values()[ordinalNumber]
    val pager = when (type) {
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
                onClick = navigateBack
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.back)
                )
            }
        })
    }) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(120.dp),
            contentPadding = PaddingValues(horizontal = Dimens.PaddingSmall),
            modifier = Modifier.padding(top = it.calculateTopPadding())
        ) {
            items(pager.itemCount) { index ->
                val media = pager[index]
//                if (media != null) {
                    Column(modifier = Modifier
                        .clickable {
                            navigateToDetails(media?.id ?: -1)
                        }
//                        .background(shimmerBrush(media != null))
                    ) {
                        AsyncImageRoundedCorners(
                            coverImage = media?.coverImage ?: "",
                            contentDescription = "Cover of ${media?.title ?: ""}"
                        )
                        Text(
                            text = media?.title ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = Dimens.PaddingSmall, end = Dimens.PaddingSmall, bottom = Dimens.PaddingNormal)
                        )
                    }
//                }
            }
        }

    }
}