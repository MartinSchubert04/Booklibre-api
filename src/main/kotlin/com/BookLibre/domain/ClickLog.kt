package com.BookLibre.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "clicks")
class ClickLog(
    val username: String,
    val bookId: String,
    val bookTitle: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    @Id
    var id: String? = null
}
