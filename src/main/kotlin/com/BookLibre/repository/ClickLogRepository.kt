package com.BookLibre.repository

import com.BookLibre.domain.ClickLog
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ClickLogRepository : MongoRepository<ClickLog, String> {
    fun findByBookId(bookId: String): List<ClickLog>
    fun countByBookId(bookId: String): Long
    fun findByBookIdOrderByTimestampDesc(bookId: String): List<ClickLog>

    @Query("{ 'bookId': { '\$in': ?0 } }")
    fun findByBookIdIn(bookIds: List<String>): List<ClickLog>

    @Aggregation(pipeline = [
        "{ '\$match': { 'bookId': { '\$in': ?0 } } }",
        "{ '\$group': { '_id': '\$bookId', 'count': { '\$sum': 1 } } }"
    ])
    fun countGroupedByBookIdIn(bookIds: List<String>): List<BookClickCount>
}

data class BookClickCount(val id: String, val count: Int)
