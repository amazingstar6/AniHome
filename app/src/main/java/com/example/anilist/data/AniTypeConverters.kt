package com.example.anilist.data

import com.example.anilist.data.models.AniCharacterRole
import com.example.anilist.data.models.RelationTypes
import com.example.anilist.data.models.Season
import com.example.anilist.type.CharacterRole
import com.example.anilist.type.MediaRelation
import com.example.anilist.type.MediaSeason
import com.example.anilist.type.MediaType

fun CharacterRole?.toAniRole(): AniCharacterRole {
    return when (this) {
        CharacterRole.MAIN -> AniCharacterRole.MAIN
        CharacterRole.SUPPORTING -> AniCharacterRole.SUPPORTING
        CharacterRole.BACKGROUND -> AniCharacterRole.BACKGROUND
        CharacterRole.UNKNOWN__ -> AniCharacterRole.UNKNOWN
        null -> AniCharacterRole.UNKNOWN
    }
}

fun MediaRelation?.toAniRelation(): RelationTypes {
    return when (this) {
        MediaRelation.ADAPTATION -> RelationTypes.ADAPTION
        MediaRelation.PREQUEL -> RelationTypes.PREQUEL
        MediaRelation.SEQUEL -> RelationTypes.SEQUEL
        MediaRelation.PARENT -> RelationTypes.PARENT
        MediaRelation.SIDE_STORY -> RelationTypes.SIDE_STORY
        MediaRelation.CHARACTER -> RelationTypes.CHARACTER
        MediaRelation.SUMMARY -> RelationTypes.SUMMARY
        MediaRelation.ALTERNATIVE -> RelationTypes.ALTERNATIVE
        MediaRelation.SPIN_OFF -> RelationTypes.SPIN_OFF
        MediaRelation.OTHER -> RelationTypes.OTHER
        MediaRelation.SOURCE -> RelationTypes.SOURCE
        MediaRelation.COMPILATION -> RelationTypes.COMPILATION
        MediaRelation.CONTAINS -> RelationTypes.CONTAINS
        MediaRelation.UNKNOWN__ -> RelationTypes.UNKNOWN
        null -> RelationTypes.UNKNOWN
    }
}

fun MediaSeason.toAniHomeSeason(): Season {
    return when (this) {
        MediaSeason.SPRING -> Season.SPRING
        MediaSeason.SUMMER -> Season.SUMMER
        MediaSeason.FALL -> Season.FALL
        MediaSeason.WINTER -> Season.WINTER
        MediaSeason.UNKNOWN__ -> Season.UNKNOWN
    }
}

fun MediaType.toAniHomeType(): com.example.anilist.data.models.MediaType {
    return when (this) {
        MediaType.MANGA -> com.example.anilist.data.models.MediaType.MANGA
        MediaType.ANIME -> com.example.anilist.data.models.MediaType.ANIME
        MediaType.UNKNOWN__ -> com.example.anilist.data.models.MediaType.UNKNOWN
    }
}