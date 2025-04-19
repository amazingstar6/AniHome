package com.kevin.anihome.data.models

import android.content.Context
import com.kevin.anihome.R

enum class AniMediaListSort {
    MEDIA_ID,
    MEDIA_ID_DESC,
    SCORE,
    SCORE_DESC,

//    STATUS,
//    STATUS_DESC,
    PROGRESS,
    PROGRESS_DESC,
    PROGRESS_VOLUMES,
    PROGRESS_VOLUMES_DESC,
    REPEAT,
    REPEAT_DESC,
    PRIORITY,
    PRIORITY_DESC,
    STARTED_ON,
    STARTED_ON_DESC,
    FINISHED_ON,
    FINISHED_ON_DESC,
    ADDED_TIME,
    ADDED_TIME_DESC,
    UPDATED_TIME,
    UPDATED_TIME_DESC,

    // only using user preferred title
    MEDIA_TITLE,
    MEDIA_TITLE_DESC,
    ;

    // todo requires reload
//    MEDIA_POPULARITY,
//    MEDIA_POPULARITY_DESC;

    fun toString(context: Context): String {
        return when (this) {
            // desc strings are not used for now
            MEDIA_ID -> context.getString(R.string.media_id)
            MEDIA_ID_DESC -> context.getString(R.string.media_id_desc)
            SCORE -> context.getString(R.string.score)
            SCORE_DESC -> context.getString(R.string.score_desc)
            PROGRESS -> context.getString(R.string.progress)
            PROGRESS_DESC -> context.getString(R.string.progress_desc)
            PROGRESS_VOLUMES -> context.getString(R.string.progress_volumes)
            PROGRESS_VOLUMES_DESC -> context.getString(R.string.progress_volumes_desc)
            REPEAT -> context.getString(R.string.repeat)
            REPEAT_DESC -> context.getString(R.string.repeat_desc)
            PRIORITY -> context.getString(R.string.priority)
            PRIORITY_DESC -> context.getString(R.string.priority_desc)
            STARTED_ON -> context.getString(R.string.started_on)
            STARTED_ON_DESC -> context.getString(R.string.started_on_desc)
            FINISHED_ON -> context.getString(R.string.finished_on)
            FINISHED_ON_DESC -> context.getString(R.string.finished_on_desc)
            ADDED_TIME -> context.getString(R.string.added_time)
            ADDED_TIME_DESC -> context.getString(R.string.added_time_desc)
            UPDATED_TIME -> context.getString(R.string.updated_time)
            UPDATED_TIME_DESC -> context.getString(R.string.updated_time_desc)
            MEDIA_TITLE -> context.getString(R.string.media_title)
            MEDIA_TITLE_DESC -> context.getString(R.string.media_title_desc)
        }
    }

    fun removeDescending(): AniMediaListSort {
        return AniMediaListSort.valueOf(this.name.substringBefore("_DESC"))
    }

    fun isDescending(): Boolean {
        return this.name.contains("_DESC")
    }
}
