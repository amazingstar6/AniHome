package com.kevin.anihome.data.models

import android.content.Context
import com.kevin.anihome.R

data class CharacterWithVoiceActor(
    val id: Int = -1,
    val voiceActorId: Int = -1,
    val name: String = "",
    val coverImage: String = "",
    val voiceActorName: String = "",
    val voiceActorCoverImage: String = "",
    val voiceActorLanguage: String = "",
    val role: AniCharacterRole = AniCharacterRole.UNKNOWN,
    val roleNotes: String = "",
)

enum class AniCharacterRole {
    MAIN,
    SUPPORTING,
    BACKGROUND,
    UNKNOWN,
    ;

    fun toString(context: Context): String {
        return when (this) {
            MAIN -> context.getString(R.string.main)
            SUPPORTING -> context.getString(R.string.supporting)
            BACKGROUND -> context.getString(R.string.background)
            UNKNOWN -> context.getString(R.string.unknown)
        }
    }
}
