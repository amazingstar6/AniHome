package com.kevin.anihome.data.models

data class AniLink(
    val url: String,
    val site: String,
    val language: String,
    val color: String,
    val icon: String,
    val type: AniLinkType,
)

enum class AniLinkType {
    INFO,
    STREAMING,
    SOCIAL,
    UNKNOWN,
}
