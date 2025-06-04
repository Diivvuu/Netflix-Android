package com.example.myapplication.ui.model

data class Profile(
    val id: String,
    val name: String,
    val avatarUrl: String
)

data class GenreResponse (
    val movieGenreIds : List<String>,
    val tvGenreIds : List<String>
)