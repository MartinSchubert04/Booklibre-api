package com.BookLibre.dto

import com.BookLibre.domain.BookFilter

data class SearchRequest(
    val userId: Int,
    val bookName: String,
    val filter: BookFilter,
    val sortType: String? = "title",
    val page: Int,
    val quantity: Int
)

