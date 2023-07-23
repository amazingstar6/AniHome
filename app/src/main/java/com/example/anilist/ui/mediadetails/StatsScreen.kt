package com.example.anilist.ui.mediadetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.anilist.R
import com.example.anilist.data.models.Season
import com.example.anilist.data.models.Stats
import com.example.anilist.ui.Dimens

@Composable
fun Stats(stats: Stats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.PaddingNormal)
    ) {
        Heading("Rankings")

        if (stats.highestRatedAllTime != -1 ) {
            IconWithTextRankings(
                stringResource(
                    id = R.string.highest_rated_all_time,
                    stats.highestRatedAllTime
                ),
                false
            )
        }

        if (stats.mostPopularAllTime != -1) {
            IconWithTextRankings(
                stringResource(
                    id = R.string.most_popular_all_time,
                    stats.mostPopularAllTime
                ),
                true
            )
        }

        if (stats.highestRatedYearRank != -1) {
            IconWithTextRankings(
                stringResource(
                    id = R.string.highest_rated_year,
                    stats.highestRatedYearRank,
                    stats.highestRatedYearNumber
                ),
                false
            )
        }

        if (stats.mostPopularYearRank != -1) {
            IconWithTextRankings(
                stringResource(
                    id = R.string.most_popular_year,
                    stats.mostPopularYearRank,
                    stats.mostPopularYearNumber
                ),
                true
            )
        }

        if (stats.highestRatedSeasonRank != -1 ) {
            IconWithTextRankings(
                stringResource(
                    id = R.string.highest_rated_season,
                    stats.highestRatedSeasonRank,
                    stats.highestRatedSeasonSeason.getName(),
                    stats.highestRatedSeasonYear
                ),
                false
            )
        }

        if (stats.mostPopularSeasonRank != -1 ) {
            IconWithTextRankings(
                stringResource(
                    id = R.string.most_popular_season,
                    stats.mostPopularSeasonRank,
                    stats.mostPopularSeasonSeason.getName(),
                    stats.mostPopularSeasonYear
                ),
                true
            )
        }

        Heading("Score distribution")
        Heading("Status distribution")
    }
}

@Composable
private fun Heading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun IconWithTextRankings(text: String, showHeart: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .then(Modifier)
    ) {
        Icon(
            painter = painterResource(
                id = if (showHeart) {
                    R.drawable.anime_details_heart
                } else {
                    R.drawable.anime_details_rating_star
                }
            ),
            contentDescription = text,
            tint = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatsPreview() {
    Stats(
        stats = Stats(
            highestRatedAllTime = 99,
            mostPopularAllTime = 183,
            highestRatedYearRank = 8,
            highestRatedYearNumber = 2023,
            mostPopularSeasonRank = 2,
            mostPopularSeasonYear = 2023,
            mostPopularSeasonSeason = Season.SUMMER,
            mostPopularYearNumber = 2023,
            mostPopularYearRank = 65,
            highestRatedSeasonRank = 3,
            highestRatedSeasonSeason = Season.SUMMER,
            highestRatedSeasonYear = 2023
        )
    )
}
