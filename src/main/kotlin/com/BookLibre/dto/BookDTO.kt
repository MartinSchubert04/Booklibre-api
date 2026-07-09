package com.BookLibre.dto

import com.BookLibre.domain.Book
import com.BookLibre.domain.BookProps
import com.BookLibre.domain.BookState
import com.BookLibre.domain.BookTypeStrategy
import com.BookLibre.domain.ClickLog
import com.BookLibre.domain.Reservation
import com.BookLibre.domain.User
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDate
import java.time.LocalDateTime

data class ClickLogDTO(
    val username: String,
    val bookTitle: String,
    val bookId: String,
    val timestamp: LocalDateTime
)

fun ClickLog.toDTO() = ClickLogDTO(
    username = username,
    bookTitle = bookTitle,
    bookId = bookId,
    timestamp = timestamp
)

data class BookUpdateDTO(
    @field:NotBlank(message = "El título es obligatorio")
    var title: String = "",

    @field:NotBlank(message = "La descripción es obligatoria")
    var description: String,

    @field:NotBlank(message = "El tipo es obligatorio")
    var type: String,

    @field:NotBlank(message = "El género es obligatorio")
    var genre: String = "",

    @field:Positive(message = "Las páginas deben ser mayores a 0")
    var totalPages: Int,

    @field:NotBlank(message = "La editorial es obligatoria")
    var editorial: String,

    @field:NotNull(message = "La fecha de publicación es obligatoria")
    var publicationDate: LocalDate? = null,

    @field:NotBlank(message = "El autor es obligatorio")
    var author: String = "",

    @field:NotBlank(message = "El ISBN es obligatorio")
    var isbn: String? = null,

    @field:NotBlank(message = "El idioma es obligatorio")
    var language: String = "",

    @field:NotNull(message = "El estado es obligatorio")
    var state: BookState? = null,

    var imgUrl: String = ""
)

data class BookCardDTO(
    var id: String? = null,
    var title: String = "",
    var type: String,
    var genre: String = "",
    var author: String = "",
    var isbn: String? = null,
    var language: String = "",
    var state: BookState? = null,
    var reserved: Boolean = false,
    var owner: UserCardDTO? = null,
    var imgUrl: String = "",
    var addedDate: LocalDateTime? = null,
    var rating: Double? = 0.0,
    var bibliokarmas: Int = 0
)

fun BookCardDTO.toEntity(): Book {
    return Book(BookProps(
        title = title,
        type = BookTypeStrategy.fromString(type),
        genre = genre,
        author = author,
        isbn = isbn ?: "",
        language = language,
        state = state,
        imgUrl = imgUrl,
    ))
}

data class BookRowDTO(
    var id: String? = null,
    var title: String = "",
    var type: String,
    var genre: String = "",
    var author: String = "",
    var isbn: String? = null,
    var language: String = "",
    var state: BookState? = null,
    var reserved: Boolean = false,
    var owner: UserCardDTO? = null,
    var imgUrl: String = "",
    var addedDate: LocalDateTime? = null,
    var rating: Double? = 0.0,
)

data class BookFilterRequest(
    val title: String? = null,
    val genre: String? = null,
    val language: String? = null,
    val type: String? = null,
    val condition: String? = null,
    val owner: String? = null,
    val isbn: String? = null,
    val minPages: Int? = null,
    val maxPages: Int? = null,
    val availableFrom: LocalDate? = null,
    val availableTo: LocalDate? = null,
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "relevance",
    val sortDir: String = "desc",
)

data class BookDetailDTO(
    var id: String? = null,
    var title: String = "",
    var description: String,
    var type: String,
    var genre: String = "",
    var totalPages: Int,
    var editorial: String,
    var publicationDate: LocalDate? = null,
    var author: String = "",
    var isbn: String? = null,
    var language: String = "",
    var state: BookState? = null,
    var ranking: Double,
    val bibliokarmas: Int,
    val recentReviews: List<ReviewDTO>,
    var imgUrl: String = ""
)

data class ReviewDTO(
    val reviewerName: String,
    val comment: String,
    val rating: Double
)

data class ReservationRequestDTO(
    val fechaDesde: String,
    val fechaHasta: String
)

data class BookCreateDTO(
    var title: String = "",
    var type: String? = null,
    var description: String = "",
    var genre: String = "",
    var author: String = "",
    var totalPages: Int = 0,
    var isbn: String = "",
    var language: String = "",
    var publicationDate: LocalDate? = null,
    var state: BookState? = null,
    var editorial: String = "",
    var imgUrl: String = ""
)

// =========================
// Extension functions
// =========================

// Movido desde Book.kt — el dominio no debe conocer los DTOs
fun Book.updateFromDTO(dto: BookUpdateDTO) {
    // Accedemos via setters del dominio, no props directamente
    val updatedProps = BookProps().apply {
        title = dto.title
        author = dto.author
        description = dto.description
        genre = dto.genre
        totalPages = dto.totalPages
        language = dto.language
        editorial = dto.editorial
        publicationDate = dto.publicationDate ?: this@updateFromDTO.getPublicationDate()
        isbn = dto.isbn ?: ""
        state = dto.state
        imgUrl = if (dto.imgUrl.isNotBlank()) dto.imgUrl else this@updateFromDTO.getImgUrl()
        owner = this@updateFromDTO.getOwner()
        reservations = this@updateFromDTO.getReservations()
        type = BookTypeStrategy.fromString(dto.type)
    }
    this.applyProps(updatedProps)
}

fun Book.toCardDTO(): BookCardDTO {
    return BookCardDTO(
        id = id,
        title = getTitle(),
        type = getType().getTypeName(),
        genre = getGenre(),
        author = getAuthor(),
        isbn = getIsbn(),
        language = getLanguage(),
        state = getState(),
        owner = getOwner()?.toCardDTO(),
        imgUrl = getImgUrl(),
        addedDate = getAddedDate()
//        rating = rating,  aplicado en service
//        bibliokarmas = 0  se calcula en el service
    )
}

fun Book.toRowDTO(): BookRowDTO {
    return BookRowDTO(
        id = this.id,
        title = this.getTitle(),
        type = this.getType().getTypeName(),
        genre = this.getGenre(),
        author = this.getAuthor(),
        isbn = this.getIsbn(),
        language = this.getLanguage(),
        state = this.getState(),
        reserved = this.getReserved(),
        owner = this.getOwner()?.toCardDTO(),
        imgUrl = this.getImgUrl(),
        addedDate = this.getAddedDate(),
        rating = this.getRating(),
    )
}

fun Book.toDetailDTO(bibliokarmasCalculados: Int, reviews: List<ReviewDTO>, rankingPromedio: Double): BookDetailDTO {
    return BookDetailDTO(
        id = this.id,
        title = this.getTitle(),
        author = this.getAuthor(),
        description = this.getDescription(),
        type = this.getType().getTypeName(),
        genre = this.getGenre(),
        totalPages = this.getTotalPages(),
        language = this.getLanguage(),
        editorial = this.getEditorial(),
        publicationDate = this.getPublicationDate(),
        isbn = this.getIsbn(),
        state = this.getState(),
        ranking = rankingPromedio,
        bibliokarmas = bibliokarmasCalculados,
        recentReviews = reviews,
        imgUrl = this.getImgUrl()
    )
}

fun Book.toGetInfoBookUpdateDTO(): BookUpdateDTO {
    return BookUpdateDTO(
        title = this.getTitle(),
        author = this.getAuthor(),
        description = this.getDescription(),
        type = this.getType().getTypeName(),
        genre = this.getGenre(),
        totalPages = this.getTotalPages(),
        language = this.getLanguage(),
        editorial = this.getEditorial(),
        publicationDate = this.getPublicationDate(),
        isbn = this.getIsbn(),
        state = this.getState(),
        imgUrl = this.getImgUrl()
    )
}