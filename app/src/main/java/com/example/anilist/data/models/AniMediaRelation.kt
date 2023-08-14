package com.example.anilist.data.models

import android.content.Context
import com.example.anilist.R

data class AniMediaRelation(
    val id: Int,
    val coverImage: String,
    val title: String,
    val relation: AniMediaRelationTypes,
)

enum class AniMediaRelationTypes {
    ADAPTION,
    PREQUEL,
    SEQUEL,
    PARENT,
    SIDE_STORY,
    CHARACTER,
    SUMMARY,
    ALTERNATIVE,
    SPIN_OFF,
    OTHER,
    SOURCE,
    COMPILATION,
    CONTAINS,
    UNKNOWN;

    fun toString(context: Context): String {
        return when (this) {
            ADAPTION -> context.getString(R.string.adaption)
            PREQUEL -> context.getString(R.string.prequel)
            SEQUEL -> context.getString(R.string.sequel)
            PARENT -> context.getString(R.string.parent)
            SIDE_STORY -> context.getString(R.string.side_story)
            CHARACTER -> context.getString(R.string.character)
            SUMMARY -> context.getString(R.string.summary)
            ALTERNATIVE -> context.getString(R.string.alternative)
            SPIN_OFF -> context.getString(R.string.spin_off)
            OTHER -> context.getString(R.string.other)
            SOURCE -> context.getString(R.string.source)
            COMPILATION -> context.getString(R.string.compilation)
            CONTAINS -> context.getString(R.string.contains)
            UNKNOWN -> context.getString(R.string.unknown)
        }
    }
}
