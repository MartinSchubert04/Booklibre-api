package com.BookLibre.domain

data class Dashboard(
    val popularBooks: List<Book>? = null,
    val totalUsers: Int? = null,
    val totalReservations: Int? = null
)