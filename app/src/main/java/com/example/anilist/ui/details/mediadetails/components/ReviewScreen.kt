package com.example.anilist.ui.details.mediadetails.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilist.R
import com.example.anilist.data.models.AniReview
import com.example.anilist.data.models.AniReviewRatingStatus
import com.example.anilist.ui.Dimens
import com.example.anilist.ui.details.reviewdetail.AvatarNameDate
import com.example.anilist.utils.Utils
import de.charlex.compose.HtmlText
import timber.log.Timber


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Reviews(
    reviews: LazyPagingItems<AniReview>,
    vote: (rating: AniReviewRatingStatus, reviewId: Int) -> Unit,
    onNavigateToReviewDetails: (Int) -> Unit
) {
    if (reviews.itemCount != 0) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(reviews.itemCount) { index ->
                val review = reviews[index]
                if (review != null) {
                    Card(
                        onClick = { onNavigateToReviewDetails(review.id) },
                        modifier = Modifier.padding(
                            top = Dimens.PaddingNormal,
                            bottom = Dimens.PaddingSmall,
                            start = Dimens.PaddingNormal,
                            end = Dimens.PaddingNormal,
                        ),
                    ) {
                        AvatarNameDate(
                            avatar = review.userAvatar,
                            userName = review.userName,
                            date = Utils.getRelativeTime(review.createdAt.toLong()),
                            modifier = Modifier.padding(Dimens.PaddingSmall),
                        )
                        Text(
                            text = review.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = Dimens.PaddingNormal),
                        )
                        HtmlText(
                            text = review.body,
                            maxLines = 7,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    colorMapping = mapOf(Color.Black to MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier.padding(Dimens.PaddingNormal),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .padding(horizontal = Dimens.PaddingNormal)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Timber.d("This review ${review.title} has user rating " + review.userRating)
                        UpDownVote(
                            review.upvotes,
                            if (review.userRating == AniReviewRatingStatus.UP_VOTE) R.drawable.media_detail_thumbs_up_filled else R.drawable.media_detail_thumbs_up_outlined,
                            "upvote",
                            vote = {
                                vote(
                                    if (review.userRating == AniReviewRatingStatus.UP_VOTE) AniReviewRatingStatus.NO_VOTE else AniReviewRatingStatus.UP_VOTE,
                                    review.id
                                )
                            }
                        )
                        UpDownVote(
                            review.totalVotes - review.upvotes,
                            iconId = if (review.userRating == AniReviewRatingStatus.DOWN_VOTE) R.drawable.media_detail_thumbs_down_filled else R.drawable.media_detail_thumb_down_outlined,
                            contentDescription = "downvote",
                            modifier = Modifier.weight(1f),
                            vote = {
                                vote(
                                    if (review.userRating == AniReviewRatingStatus.DOWN_VOTE) AniReviewRatingStatus.NO_VOTE else AniReviewRatingStatus.DOWN_VOTE,
                                    review.id
                                )
                            }
                        )
                        Text(
                            text = "${review.score}/100",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = Dimens.PaddingSmall),
                        )
                    }
                }
            }
        }
    } else {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.no_reviews),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun Avatar(avatar: String, userName: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(avatar).crossfade(true).build(),
        placeholder = painterResource(id = R.drawable.no_image),
        contentDescription = "avatar of $userName",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .clip(CircleShape)
            .size(60.dp),
    )
}

@Composable
fun UpDownVote(
    totalVotes: Int,
    iconId: Int,
    contentDescription: String,
    vote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        IconButton(onClick = { vote() }) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = contentDescription,
            )
        }
        Text(text = totalVotes.toString(), modifier = Modifier.padding(end = Dimens.PaddingSmall))
    }
}

//@Preview(showBackground = true)
//@Composable
//fun ReviewListPreview() {
//    Reviews(
//        listOf(
//            Review(
//                title = "An unbalanced, yet entertaining and explosive season of KnY",
//                userName = "SlayerArt",
//                body = "I can't fucking believe it. They've done it again (sort of).It's no secret that ufotable have been killing it with their new projects over the last few years. The Heaven's Feel trilogy is stunning, and the two previous seasons of Demon Slayer were superb in the animation",
//                upvotes = 43,
//                totalVotes = 64,
//                score = 80,
//                createdAt = 1533109209,
//            ),
//        ),
//    ) { }
//}

//@Preview(showBackground = true)
//@Composable
//fun NoReviewsPreview() {
//    Reviews(
//        emptyList(),
//    ) { }
//}
