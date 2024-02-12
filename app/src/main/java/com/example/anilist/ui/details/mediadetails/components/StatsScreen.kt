package com.example.anilist.ui.details.mediadetails.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.example.anilist.R
import com.example.anilist.data.models.AniScoreDistribution
import com.example.anilist.data.models.AniSeason
import com.example.anilist.data.models.AniStats
import com.example.anilist.data.models.AniStatsStatusDistribution
import com.example.anilist.ui.Dimens
import com.example.anilist.ui.theme.AnilistTheme
import com.patrykandpatrick.vico.compose.axis.axisGuidelineComponent
import com.patrykandpatrick.vico.compose.axis.axisLineComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun Stats(stats: AniStats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.PaddingNormal),
    ) {
        if (stats.ranksIsNotEmpty) {
            Heading(stringResource(R.string.rankings))
            Rankings(stats)
        }

        Heading(stringResource(R.string.score_distribution))
        val scoreDistribution = stats.scoreDistribution
        val chartEntryModel = entryModelOf(
            10 to scoreDistribution.ten,
            20 to scoreDistribution.twenty,
            30 to scoreDistribution.thirty,
            40 to scoreDistribution.forty,
            50 to scoreDistribution.fifty,
            60 to scoreDistribution.sixty,
            70 to scoreDistribution.seventy,
            80 to scoreDistribution.eighty,
            90 to scoreDistribution.ninety,
            100 to scoreDistribution.hundred,
        )
        ProvideChartStyle(m3ChartStyle()) {
            Chart(
                chart = columnChart(
                    dataLabel = textComponent(color = MaterialTheme.colorScheme.onSurface),
                    dataLabelVerticalPosition = VerticalPosition.Top,
                ),
                model = chartEntryModel,
                bottomAxis = bottomAxis(
                    axis = axisLineComponent(),
                    label = textComponent(color = MaterialTheme.colorScheme.onSurface),
                    guideline = axisGuidelineComponent(thickness = 0.dp),
                ),
            )
        }
        Heading(stringResource(R.string.status_distribution))
        val statusDistribution = stats.statusDistribution

        val listToSortValues: List<Pair<AniStatsStatusTypes, Int>> =
            listOf(
                Pair(AniStatsStatusTypes.CURRENT, statusDistribution.current),
                Pair(AniStatsStatusTypes.COMPLETED, statusDistribution.completed),
                Pair(AniStatsStatusTypes.PLANNING, statusDistribution.planning),
                Pair(AniStatsStatusTypes.PAUSED, statusDistribution.paused),
                Pair(AniStatsStatusTypes.DROPPED, statusDistribution.dropped),
            ).sortedByDescending { it.second }

        //todo sort by largest first
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.PaddingSmall),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listToSortValues.forEach {
                StatsLegendText(
                    text = it.first.toString(LocalContext.current),
                    it.first.getColor(LocalContext.current),
                    it.second
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            listToSortValues.forEach {
                HorizontalDivider(
                    modifier = Modifier.weight(
                        it.second.toFloat()
                    ), thickness = 12.dp, color = it.first.getColor(LocalContext.current)
                )
            }
        }
    }
}

/**
 * Only shows the two most highest ones when onlyShowTwo is true.
 * So an element is shown when onlyShowTwo is true and heart/starShown is true, this gives the truth table:
 *
 * | onlyShowTwo | heart/starShown | output |
 * |-------------|-----------------|--------|
 * | 0           | 0               | 1      |
 * | 0           | 1               | 1      |
 * | 1           | 0               | 1      |
 * | 1           | 1               | 0      |
 */
@Composable
fun Rankings(stats: AniStats, onlyShowTwo: Boolean = false, modifier: Modifier = Modifier) {
    var heartShown = false
    var starShown = false
    Column(modifier = modifier) {
        if (stats.highestRatedAllTime != -1) {
            starShown = true
            IconWithTextRankings(
                stringResource(
                    id = R.string.highest_rated_all_time,
                    stats.highestRatedAllTime,
                ),
                false,
            )
        }

        if (stats.mostPopularAllTime != -1) {
            heartShown = true
            IconWithTextRankings(
                stringResource(
                    id = R.string.most_popular_all_time,
                    stats.mostPopularAllTime,
                ),
                true,
            )
        }

        if (stats.highestRatedYearRank != -1 && !(onlyShowTwo && starShown)) {
            starShown = true
            IconWithTextRankings(
                stringResource(
                    id = R.string.highest_rated_year,
                    stats.highestRatedYearRank,
                    stats.highestRatedYearNumber,
                ),
                false,
            )
        }

        if (stats.mostPopularYearRank != -1 && !(onlyShowTwo && heartShown)) {
            heartShown = true
            IconWithTextRankings(
                stringResource(
                    id = R.string.most_popular_year,
                    stats.mostPopularYearRank,
                    stats.mostPopularYearNumber,
                ),
                true,
            )
        }

        if (stats.highestRatedSeasonRank != -1 && !(onlyShowTwo && starShown)) {
            starShown = true
            IconWithTextRankings(
                stringResource(
                    id = R.string.highest_rated_season,
                    stats.highestRatedSeasonRank,
                    stats.highestRatedSeasonSeason.getString(LocalContext.current),
                    stats.highestRatedSeasonYear,
                ),
                false,
            )
        }

        if (stats.mostPopularSeasonRank != -1 && !(onlyShowTwo && heartShown)) {
            heartShown = true
            IconWithTextRankings(
                stringResource(
                    id = R.string.most_popular_season,
                    stats.mostPopularSeasonRank,
                    stats.mostPopularSeasonSeason.getString(LocalContext.current),
                    stats.mostPopularSeasonYear,
                ),
                true,
            )
        }
    }
}

@Composable
private fun StatsLegendText(
    text: String,
    color: Color,
    amount: Int,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(color = color, shape = MaterialTheme.shapes.medium) {
            Text(
                text = text,
                color = Color.White,
                modifier = Modifier
                    .padding(4.dp)
                    .then(modifier)
            )
        }
        Text(
            text = amount.toString(),
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun Heading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
fun IconWithTextRankings(text: String, showHeart: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .then(Modifier),
    ) {
        Icon(
            painter = painterResource(
                id = if (showHeart) {
                    R.drawable.anime_details_heart
                } else {
                    R.drawable.anime_details_rating_star
                },
            ),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
        )
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatsPreview() {
    AnilistTheme {
        Surface {
            Stats(
                stats = AniStats(
                    highestRatedAllTime = 99,
                    mostPopularAllTime = 183,
                    highestRatedYearRank = 8,
                    highestRatedYearNumber = 2023,
                    mostPopularSeasonRank = 2,
                    mostPopularSeasonYear = 2023,
                    mostPopularSeasonSeason = AniSeason.SUMMER,
                    mostPopularYearNumber = 2023,
                    mostPopularYearRank = 65,
                    highestRatedSeasonRank = 3,
                    highestRatedSeasonSeason = AniSeason.SUMMER,
                    highestRatedSeasonYear = 2023,
                    scoreDistribution = AniScoreDistribution(
                        105,
                        34, 28, 28, 102, 143, 627, 1511, 3009, 2437,
                    ),
                    statusDistribution = AniStatsStatusDistribution(
                        current = 279069,
                        planning = 67133,
                        completed = 20648,
                        dropped = 29865,
                        paused = 56446,
                    )
//            mapOf(
//                Status.CURRENT to 279069,
//                Status.COMPLETED to 20648,
//                Status.PLANNING to 67133,
//                Status.DROPPED to 29865,
//                Status.PAUSED to 56446,
//            ),
                ),
            )
        }
    }
}

enum class AniStatsStatusTypes {
    CURRENT,
    PLANNING,
    PAUSED,
    DROPPED,
    COMPLETED;

    fun toString(context: Context): String {
        return when (this) {
            CURRENT -> context.getString(R.string.current)
            PLANNING -> context.getString(R.string.planning)
            PAUSED -> context.getString(R.string.paused)
            DROPPED -> context.getString(R.string.dropped)
            COMPLETED -> context.getString(R.string.completed)
        }
    }

    fun getColor(context: Context): Color {
        return when (this) {
            CURRENT -> Color(context.getColor(R.color.purple_200))
            PLANNING -> Color(context.getColor(R.color.green_200))
            PAUSED -> Color(context.getColor(R.color.blue_200))
            DROPPED -> Color(context.getColor(R.color.pink_200))
            COMPLETED -> Color(context.getColor(R.color.red_200))
        }
    }
}