package com.BookLibre.service

import com.BookLibre.domain.Book
import com.BookLibre.domain.ClickLog
import com.BookLibre.dto.BookCardDTO
import com.BookLibre.dto.toCardDTO
import com.BookLibre.repository.BookRepository
import com.BookLibre.repository.ClickLogRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class ClickService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val clickLogRepository: ClickLogRepository,
    private val bookRepository: BookRepository,
    private val objectMapper: ObjectMapper
) {
    companion object {
        const val TOP_BOOKS_KEY = "books:popular"
        const val BOOKS_DATA_PREFIX = "books:data:"
        const val TOP_LIMIT = 10L
        const val MIN_CACHED = 6
        val BOOKS_TTL: Duration = Duration.ofHours(1)
    }

    @Async
    fun registerClick(click: ClickLog, book: Book) {
        clickLogRepository.save(click)
        redisTemplate.opsForZSet().incrementScore(TOP_BOOKS_KEY, click.bookId, 1.0)
        cacheBookDTO(book.toCardDTO())
    }

    @Async
    fun saveBooks(books: List<BookCardDTO>) {
        books.forEach { cacheBookDTO(it) }
    }

    private fun cacheBookDTO(dto: BookCardDTO) {
        val id = dto.id ?: return
        redisTemplate.opsForValue().set(
            "$BOOKS_DATA_PREFIX$id",
            objectMapper.writeValueAsString(dto),
            BOOKS_TTL
        )
    }

    fun getTopBooks(): List<BookCardDTO> {
        val topIds = redisTemplate.opsForZSet()
            .reverseRange(TOP_BOOKS_KEY, 0, TOP_LIMIT - 1)
            ?.map { it.toString() }
            ?: return emptyList()

        val cachedBooks = topIds.mapNotNull { id ->
            val json = redisTemplate.opsForValue().get("$BOOKS_DATA_PREFIX$id") as? String
            json?.let { objectMapper.readValue(it, BookCardDTO::class.java) }
        }

        if (cachedBooks.size >= MIN_CACHED) return cachedBooks

        val cachedIds = cachedBooks.mapNotNull { it.id }.toSet()
        val missingIds = topIds.filter { it !in cachedIds }
        val fromMongo = bookRepository.findAllById(missingIds).map { it.toCardDTO() }
        fromMongo.forEach { cacheBookDTO(it) }

        return cachedBooks + fromMongo
    }

    fun getTopWithScores(limit: Long = 5): List<TopClickedBook> {
        val tuples = redisTemplate.opsForZSet()
            .reverseRangeWithScores(TOP_BOOKS_KEY, 0, limit - 1)
            ?: return emptyList()

        return tuples.mapNotNull { tuple ->
            val bookId = tuple.value?.toString() ?: return@mapNotNull null
            val clicks = tuple.score?.toInt() ?: 0
            val json = redisTemplate.opsForValue().get("$BOOKS_DATA_PREFIX$bookId") as? String
            val title = json?.let { objectMapper.readValue(it, BookCardDTO::class.java).title } ?: ""
            TopClickedBook(bookId, title, clicks)
        }
    }
}

data class TopClickedBook(
    val bookId: String,
    val title: String,
    val clicks: Int
)
