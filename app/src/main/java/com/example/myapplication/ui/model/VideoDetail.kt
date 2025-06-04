package com.example.myapplication.ui.model

data class VideoDetail(
    val id: String,
    val type: String,
    val title: String,
    val description: String,
    val posterUrl: String,
    val backdropUrl: String,
    val releaseDate: String?,
    val genres: List<String>,
    val rating: Double,
    val cast: List<CastMember>,
    val seasons: List<Season> = emptyList()
)

data class CastMember(
    val name: String,
    val character: String,
    val profile: String?
)

data class Season(
    val seasonNumber: Int,
    val episodeCount: Int,
    val name: String,
    val poster: String?
)
