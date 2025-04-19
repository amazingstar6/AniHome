package com.kevin.anihome.data.models

/**
 * Entry list id is necessary for updating, media Id should be there for creating
 */
data class StatusUpdate(
    val entryListId: Int,
    val mediaId: Int,
    val status: AniPersonalMediaStatus?,
    val scoreRaw: Int?,
    val progress: Int?,
    val progressVolumes: Int?,
    val repeat: Int?,
    val priority: Int?,
    val privateToUser: Boolean?,
    val notes: String?,
    val hiddenFromStatusList: Boolean?,
    val customLists: List<String>?,
    val advancedScores: List<Double>?,
    val startedAt: FuzzyDate?,
    val completedAt: FuzzyDate?,
)
