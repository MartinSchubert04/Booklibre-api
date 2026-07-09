package com.BookLibre.dto

data class SearchResponse (
    val books: List<BookCardDTO>,
    val totalPages: Int
)

data class ProfileResponse(
    val user: UserProfileDTO,
    val clicks: Map<String, Int>
)