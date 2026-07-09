package com.BookLibre.domain

import com.BookLibre.error.BusinessException

val sortStrategies = mapOf(
    "title" to SortTitle(),
    "author" to SortAuthor(),
    "user" to SortOwner()
)


interface SortStrategy {
    fun comparator(): Comparator<Book>
}

class SortTitle : SortStrategy {
    override fun comparator() = compareBy<Book> { it.getTitle() }
}

class SortAuthor : SortStrategy {
    override fun comparator() = compareBy<Book> { it.getAuthor() }
}

class SortOwner : SortStrategy {
    override fun comparator() = compareBy<Book> {
        it.getOwner()?.getFullName() ?: "Z - Sin dueño"
    }
}