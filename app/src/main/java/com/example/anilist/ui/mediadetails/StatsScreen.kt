package com.example.anilist.ui.mediadetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.anilist.R
import com.example.anilist.data.models.ScoreDistribution
import com.example.anilist.data.models.Season
import com.example.anilist.data.models.Stats
import com.example.anilist.data.models.Status
import com.example.anilist.ui.Dimens
import com.patrykandpatrick.vico.compose.axis.axisGuidelineComponent
import com.patrykandpatrick.vico.compose.axis.axisLineComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun Stats(stats: Stats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.PaddingNormal),
    ) {
        if (stats.ranksIsNotEmpty) {
            Heading("Rankings")
            Rankings(stats)
        }

        Heading("Score distribution")
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
//                    valueFormatter = AxisValueFormatter {
//                            value, chartValues
//                        ->
//                        "$chartValues"
//                    },
                    guideline = axisGuidelineComponent(thickness = 0.dp),
                ),
//                marker = markerComponent(
//                    label = textComponent(MaterialTheme.colorScheme.onSurface),
//                    indicator = shapeComponent(Shapes.pillShape, MaterialTheme.colorScheme.surface),
//                    guideline = axisGuidelineComponent()
//                ),
            )
        }
        Heading("Status distribution")
        val statusDistribution = stats.statusDistribution
        val total = statusDistribution.values.sum()
        val currentColor = Color.Red
        val planningColor = Color.Black
        val completedColor = Color.Green
        val droppedColor = Color.Magenta
        val pausedColor = Color.Gray

        Row {
            StatsLegendText(text = "Completed", completedColor)
            StatsLegendText(text = "Current", currentColor)
            StatsLegendText(text = "Planning", planningColor)
            StatsLegendText(text = "Paused", pausedColor)
            StatsLegendText(text = "Dropped", droppedColor)
        }
        Row {
            HorizontalDivider(
                modifier = Modifier.weight(
                    (
                            total / (
                                    statusDistribution[Status.COMPLETED]?.toFloat()
                                        ?: 1f
                                    )
                            ),
                ),
                thickness = 12.dp,
                color = completedColor
            )
            HorizontalDivider(
                modifier = Modifier.weight(
                    total / (statusDistribution[Status.CURRENT]?.toFloat() ?: 1f),
                ),
                thickness = 12.dp,
                color = currentColor
            )
            HorizontalDivider(
                modifier = Modifier.weight(
                    total / (statusDistribution[Status.PLANNING]?.toFloat() ?: 1f),
                ),
                thickness = 12.dp,
                color = planningColor
            )
            HorizontalDivider(
                modifier = Modifier.weight(
                    total / (statusDistribution[Status.PAUSED]?.toFloat() ?: 1f),
                ),
                thickness = 12.dp,
                color = pausedColor
            )
            HorizontalDivider(
                modifier = Modifier.weight(
                    total / (statusDistribution[Status.DROPPED]?.toFloat() ?: 1f),
                ),
                thickness = 12.dp,
                color = droppedColor
            )
        }
    }
}

@Composable
fun Rankings(stats: Stats, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        if (stats.highestRatedAllTime != -1) {
            IconWithTextRankings(
                stringResource(
                    id = R.string.highest_rated_all_time,
                    stats.highestRatedAllTime,
                ),
                false,
            )
        }

        if (stats.mostPopularAllTime != -1) {
            IconWithTextRankings(
                stringResource(
                    id = R.string.most_popular_all_time,
                    stats.mostPopularAllTime,
                ),
                true,
            )
        }

        if (stats.highestRatedYearRank != -1) {
            IconWithTextRankings(
                stringResource(
                    id = R.string.highest_rated_year,
                    stats.highestRatedYearRank,
                    stats.highestRatedYearNumber,
                ),
                false,
            )
        }

        if (stats.mostPopularYearRank != -1) {
            IconWithTextRankings(
                stringResource(
                    id = R.string.most_popular_year,
                    stats.mostPopularYearRank,
                    stats.mostPopularYearNumber,
                ),
                true,
            )
        }

        if (stats.highestRatedSeasonRank != -1) {
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

        if (stats.mostPopularSeasonRank != -1) {
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
private fun StatsLegendText(text: String, color: Color) {
    Text(text = text, color = color)
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
private fun IconWithTextRankings(text: String, showHeart: Boolean) {
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
            highestRatedSeasonYear = 2023,
            scoreDistribution = ScoreDistribution(
                105,
                34, 28, 28, 102, 143, 627, 1511, 3009, 2437,
            ),
            statusDistribution = mapOf(
                Status.COMPLETED to 100,
                Status.CURRENT to 230,
                Status.PLANNING to 500,
                Status.DROPPED to 54,
                Status.PAUSED to 20,
            ),
        ),
    )
}
