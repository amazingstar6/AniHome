package com.example.anilist.data.models

import com.example.anilist.ui.mymedia.MediaStatus

data class StatusUpdate(
    val entryListId: Int,
    val status: MediaStatus?,
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
