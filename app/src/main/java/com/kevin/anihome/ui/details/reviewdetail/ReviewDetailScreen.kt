package com.kevin.anihome.ui.details.reviewdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kevin.anihome.R
import com.kevin.anihome.data.models.AniReview
import com.kevin.anihome.data.models.AniReviewRatingStatus
import com.kevin.anihome.ui.Dimens
import com.kevin.anihome.ui.details.mediadetails.OpenInBrowserAndShareToolTips
import com.kevin.anihome.ui.details.mediadetails.components.Avatar
import com.kevin.anihome.ui.details.mediadetails.components.UpDownVote
import com.kevin.anihome.utils.FormattedHtmlWebView
import com.kevin.anihome.utils.LoadingCircle
import com.kevin.anihome.utils.Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDetailScreen(
    reviewId: Int,
    onNavigateBack: () -> Unit,
    reviewDetailViewModel: ReviewDetailViewModel = hiltViewModel(),
) {
    val review by reviewDetailViewModel.review.observeAsState()
    LaunchedEffect(key1 = Unit) {
        reviewDetailViewModel.fetchReview(reviewId)
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        TopAppBar(
            scrollBehavior = scrollBehavior,
            title = {
                Text(
                    text = review?.title ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.back),
                    )
                }
            },
            actions = {
                OpenInBrowserAndShareToolTips(
                    uriHandler = LocalUriHandler.current,
                    uri = "https://anilist.co/review/$reviewId",
                    context = LocalContext.current,
                )
            },
        )
    }) {
        if (review != null) {
            ReviewDetail(
                review ?: AniReview(),
                vote = { reviewDetailViewModel.rateReview(review?.id ?: -1, it) },
                modifier = Modifier.padding(top = it.calculateTopPadding()),
            )
        } else {
            LoadingCircle()
        }
    }
}

@Composable
private fun ReviewDetail(
    review: AniReview,
    vote: (AniReviewRatingStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(Dimens.PaddingNormal)
                .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = review.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
                Modifier.padding(
                    bottom = Dimens.PaddingSmall,
                    start = Dimens.PaddingNormal,
                    end = Dimens.PaddingNormal,
                ),
        )
        AvatarNameDate(
            review.userAvatar,
            review.userName,
            Utils.convertEpochToDateString(review.createdAt.toLong()),
        )
//        HtmlText(
//            text = review.body,
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
// //                    colorMapping = mapOf(Color.Black to MaterialTheme.colorScheme.onSurface),
//            modifier = Modifier.padding(top = Dimens.PaddingNormal),
//        )
        FormattedHtmlWebView(html = review.body)
        Divider(
            modifier = Modifier.padding(horizontal = 34.dp),
        )
        Text(
            "${review.score}/100",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = Dimens.PaddingSmall, top = Dimens.PaddingLarge),
        )
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            UpDownVote(
                review.upvotes,
                if (review.userRating == AniReviewRatingStatus.UP_VOTE) R.drawable.media_detail_thumbs_up_filled else R.drawable.media_detail_thumbs_up_outlined,
                "upvote",
                vote = {
                    vote(
                        if (review.userRating == AniReviewRatingStatus.UP_VOTE) AniReviewRatingStatus.NO_VOTE else AniReviewRatingStatus.UP_VOTE,
                    )
                },
            )
            UpDownVote(
                review.totalVotes - review.upvotes,
                iconId = if (review.userRating == AniReviewRatingStatus.DOWN_VOTE) R.drawable.media_detail_thumbs_down_filled else R.drawable.media_detail_thumb_down_outlined,
                contentDescription = "downvote",
                vote = {
                    vote(
                        if (review.userRating == AniReviewRatingStatus.DOWN_VOTE) AniReviewRatingStatus.NO_VOTE else AniReviewRatingStatus.DOWN_VOTE,
                    )
                },
            )
        }
    }
}

@Composable
fun AvatarNameDate(
    avatar: String,
    userName: String,
    date: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(Dimens.PaddingSmall)
                .then(modifier),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(avatar = avatar, userName = userName)
        Column(modifier = Modifier.padding(start = Dimens.PaddingNormal)) {
            Text(
                text = userName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = date,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.PaddingSmall),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewDetailScreenPreview() {
    ReviewDetail(
        AniReview(
            title = "An unbalanced, yet entertaining and explosive season of KnY",
            userName = "SlayerArt",
            body = "<p>Wow ,this is my  first review that i ever made and my english is quite bad so bare with me there (this review will be utter shit basically) i watched all one piece ova, movies and read some arcs in the manga , so trust me. This review will be some real biased shitty review which will only praise the show (okay prob not but this review will not be good)</p>\n<p>This review will contain spoilers</p>\n<p>~<br />\n''Wealth, fame, power. The man who had acquired everything in the world, the Pirate King, Gold Roger. The final words that were said at his execution sent the people to the seas.</p>\n<p>Gold Roger: &quot;My wealth and treasure? If you want it, I'll let you have it. Look for it, I left it all at that place!&quot;</p>\n<p>Men now, chasing their dreams, head towards the Grand Line. The world now enters a Great Age of Pirates!''</p>\n<p>~This is the first thing you hear when you begin watching one piece.<br />\nOne piece follows our protagonist luffy and he  wants to find , well, The one Piece! (wow what surprise )So that he can become the kaizoku ou (pirate king)</p>\n<p>~What make one piece special is how <strong><strong>fun</strong></strong> it is....<br />\nThe premise is really basic at first , but it gets to something really epic and bigger then first imagined. All of the straw hats are quirky and it is because of their quirkiness that it is so much fun to watch them doing shizz together.<br />\nAll of the straw hats are well developed though they never get much more character development past their arc , i can still say that they are well developed and have all interesting backgrounds.</p>\n<p>The world building is honestly groundbreaking , because of how it is set , one piece could indeed ''last forever'' without being boring.<br />\nEach island has their own lore , own people and own special characteristics to them. Each island looks quite wonderful.<br />\nOne piece has also it's own pirate philosophy which is really good and makes pirates more understood overall.</p>\n<p>~One piece is also not so serious which  is a good thing AND a bad thing<br />\nIt makes the world on one piece more wacky and fun but sometime not serious when it needs to be.</p>\n<p>Like how one piece is afraid to kill his characters, which, don't get me wrong , you don't especially need to kill your characters each second  , but sometimes you gotta do it to make the story more believable.... </p>\n<p>Like for exemple in the ''big war arc'' when it is literally pirate vs marines. There were only 2 casualties which is quite laughable. Also in the doflamingo arc when nobody dies which makes no sense at all. There is rarely a casualty in one piece which kinda sucks.</p>\n<p>One piece does have serious themes however , even though they get burried away since a character gonna say some dumb shit 10 minutes later (which is quite fun ngl) . Even naruto feels more serious with it's themes when One piece themes are actually well defined and explains most of how the world works and what the world problem is (aka good world building) .</p>\n<p>One piece does also have serious moments (mostly flashbacks). They can be really good and can set the mood for a fight which makes it more epic (Especially a final fight) </p>\n<p>There are also times when one piece also KILLS some characters , and since they rarely do kill a character.<br />\nThe character death is way more significant than in other shows and the deaths aren't ''meaningless'' (Well i hoped they wouldn't be since they no kill nobody).<br />\nAll of the deaths feel important and quite sad (though i never cried ,it was still sad , i mean everybody be crying and shizz)<br />\n~! personal opinion but ace's death felt WAY too forced , they could of killed the nibba at any time,marine had way more fo an advantage  but nooooooo , let's wait until he is not handcuffed and shit , and then Ace could of went away but this nibba is so fucking prideful ,he died to the most shittiest bait of all history, even luffy said to ace to stop this nonsense , MOTHERFLIPPING LUFFY , ace's death was overrated but it did bring character development to luffy and a little bit of the entire rest of the crew!, without it they wouldn't had the motivation to train and stuff.~</p>\n<p>The animation: Is really amazing..... amazingly dog shit lmfao .<br />\nThe fights can still be enjoyable though , because of the epic factor (but they could be way better , look at the one piece movies , especially the 4 last ones )</p>\n<p>But the animation is still total dog shit</p>\n<p>The art: The art changes quite often during the series and they really much deviated from the original art style which was more manga-like , though they went back to the right track in the wano arc </p>\n<p>The music: Quite good honestly , sad when it has to be , and epic when it has to be (aka the Walk lol) </p>\n<p>The pacing : HAHHAHAHAHAHAHHAHAHAAHA ...... dog shit . While the first few arc are decently paced , at episode 400 or so , the story becomes much slower which you could clearly see in the doflamingo arc. In fact it was so slow that i used to play tetris while binge watching that arc and didn't miss anything important of the story.</p>\n<p>The jokes: I found them quite fun but i actually rarely laughed at them.But i still Do really like them since it makes the world more fun and wacky</p>\n<p>OKay so go watch one piece now little boy , if you expect something REALLLLLY serious , dark and edgy. You won't find that here . But if you just want to see the wacky adventures of wacky characters in a good defined world , this is the show for you .Each one piece arc are wonderful and not boring , and even when the stakes goes higher , it never felt forced like dragon ball , in fact , it is quite entertaining(this conclusion sucked but alright , end of review , i score this 90 , it is still a wonderful experience even with all the cons i just presented because at the end of the day , i can't hate one piece... (i can just hate toei instead)</p>\n<p>Edit:I reread the review and a lot of grammar mistakes were made , i'm actually too lazy to edit them all ,so uh beware</p>",
            upvotes = 43,
            totalVotes = 64,
            score = 80,
            createdAt = 1533109209,
        ),
        vote = { },
    )
}
