package com.BookLibre.domain

import java.time.LocalDate

data class PageRange (
    val min: Int,
    val max: Int,
)

data class DateRange (
    val from: LocalDate?,
    val until: LocalDate?
)

data class BookFilter (
    val genres: List<String>,
    val pageRange: PageRange,
    val dateRange: DateRange,
    val isbn: String,
    val owner: String
)