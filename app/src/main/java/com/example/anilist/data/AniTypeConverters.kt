package com.example.anilist.data

import com.example.anilist.data.models.AniCharacterRole
import com.example.anilist.data.models.AniMediaRelationTypes
import com.example.anilist.data.models.AniNotificationType
import com.example.anilist.data.models.AniReviewRatingStatus
import com.example.anilist.data.models.AniSeason
import com.example.anilist.type.CharacterRole
import com.example.anilist.type.MediaRelation
import com.example.anilist.type.MediaSeason
import com.example.anilist.type.MediaType
import com.example.anilist.type.NotificationType
import com.example.anilist.type.ReviewRating

fun CharacterRole?.toAniRole(): AniCharacterRole {
    return when (this) {
        CharacterRole.MAIN -> AniCharacterRole.MAIN
        CharacterRole.SUPPORTING -> AniCharacterRole.SUPPORTING
        CharacterRole.BACKGROUND -> AniCharacterRole.BACKGROUND
        CharacterRole.UNKNOWN__ -> AniCharacterRole.UNKNOWN
        null -> AniCharacterRole.UNKNOWN
    }
}

fun MediaRelation?.toAniRelation(): AniMediaRelationTypes {
    return when (this) {
        MediaRelation.ADAPTATION -> AniMediaRelationTypes.ADAPTION
        MediaRelation.PREQUEL -> AniMediaRelationTypes.PREQUEL
        MediaRelation.SEQUEL -> AniMediaRelationTypes.SEQUEL
        MediaRelation.PARENT -> AniMediaRelationTypes.PARENT
        MediaRelation.SIDE_STORY -> AniMediaRelationTypes.SIDE_STORY
        MediaRelation.CHARACTER -> AniMediaRelationTypes.CHARACTER
        MediaRelation.SUMMARY -> AniMediaRelationTypes.SUMMARY
        MediaRelation.ALTERNATIVE -> AniMediaRelationTypes.ALTERNATIVE
        MediaRelation.SPIN_OFF -> AniMediaRelationTypes.SPIN_OFF
        MediaRelation.OTHER -> AniMediaRelationTypes.OTHER
        MediaRelation.SOURCE -> AniMediaRelationTypes.SOURCE
        MediaRelation.COMPILATION -> AniMediaRelationTypes.COMPILATION
        MediaRelation.CONTAINS -> AniMediaRelationTypes.CONTAINS
        MediaRelation.UNKNOWN__ -> AniMediaRelationTypes.UNKNOWN
        null -> AniMediaRelationTypes.UNKNOWN
    }
}

fun MediaSeason.toAniHomeSeason(): AniSeason {
    return when (this) {
        MediaSeason.SPRING -> AniSeason.SPRING
        MediaSeason.SUMMER -> AniSeason.SUMMER
        MediaSeason.FALL -> AniSeason.FALL
        MediaSeason.WINTER -> AniSeason.WINTER
        MediaSeason.UNKNOWN__ -> AniSeason.UNKNOWN
    }
}

fun MediaType.toAniHomeType(): com.example.anilist.data.models.AniMediaType {
    return when (this) {
        MediaType.MANGA -> com.example.anilist.data.models.AniMediaType.MANGA
        MediaType.ANIME -> com.example.anilist.data.models.AniMediaType.ANIME
        MediaType.UNKNOWN__ -> com.example.anilist.data.models.AniMediaType.UNKNOWN
    }
}

fun ReviewRating?.toAni(): AniReviewRatingStatus {
    return when (this) {
        ReviewRating.NO_VOTE -> AniReviewRatingStatus.NO_VOTE
        ReviewRating.UP_VOTE -> AniReviewRatingStatus.UP_VOTE
        ReviewRating.DOWN_VOTE -> AniReviewRatingStatus.DOWN_VOTE
        ReviewRating.UNKNOWN__ -> AniReviewRatingStatus.UNKNOWN
        null -> AniReviewRatingStatus.UNKNOWN
    }
}

fun NotificationType?.toAni(): AniNotificationType {
    return when (this) {
        NotificationType.ACTIVITY_MESSAGE -> AniNotificationType.ActivityMessageNotification
        NotificationType.ACTIVITY_REPLY -> AniNotificationType.ActivityReplyNotification
        NotificationType.FOLLOWING -> AniNotificationType.FollowingNotification
        NotificationType.ACTIVITY_MENTION -> AniNotificationType.ActivityMentionNotification
        NotificationType.THREAD_COMMENT_MENTION -> AniNotificationType.ThreadCommentMentionNotification
        NotificationType.THREAD_SUBSCRIBED -> AniNotificationType.ThreadCommentSubscribedNotification
        NotificationType.THREAD_COMMENT_REPLY -> AniNotificationType.ThreadCommentReplyNotification
        NotificationType.AIRING -> AniNotificationType.AiringNotification
        NotificationType.ACTIVITY_LIKE -> AniNotificationType.ActivityLikeNotification
        NotificationType.ACTIVITY_REPLY_LIKE -> AniNotificationType.ActivityReplyLikeNotification
        NotificationType.THREAD_LIKE -> AniNotificationType.ThreadLikeNotification
        NotificationType.THREAD_COMMENT_LIKE -> AniNotificationType.ThreadCommentLikeNotification
        NotificationType.ACTIVITY_REPLY_SUBSCRIBED -> AniNotificationType.ActivityReplySubscribedNotification
        NotificationType.RELATED_MEDIA_ADDITION -> AniNotificationType.RelatedMediaAdditionNotification
        NotificationType.MEDIA_DATA_CHANGE -> AniNotificationType.MediaDataChangeNotification
        NotificationType.MEDIA_MERGE -> AniNotificationType.MediaMergeNotification
        NotificationType.MEDIA_DELETION -> AniNotificationType.MediaDeletionNotification
        NotificationType.UNKNOWN__ -> AniNotificationType.UNKNOWN
        null -> AniNotificationType.UNKNOWN
    }
}
