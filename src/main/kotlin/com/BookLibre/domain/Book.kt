package com.BookLibre.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

enum class BookState {
    EXCELENTE,
    MUY_BUENO,
    BUENO,
    MALO,
    REGULAR
}

// Snapshot de una reserva embebida dentro del documento Book en MongoDB
data class ReservationEmbed(
    val id: Int? = null,
    val from: LocalDate = LocalDate.now(),
    val until: LocalDate = LocalDate.now(),
    val rating: Double? = null,
    val comment: String? = null
)

// Sin @Embeddable: en MongoDB es un subdocumento anidado sin anotaciones JPA
data class BookProps(
    var title: String = "",
    var description: String = "",
    var genre: String = "",
    var author: String = "",
    var totalPages: Int = 0,
    var isbn: String = "",
    var language: String = "",
    var publicationDate: LocalDate? = null,
    var editorial: String = "",
    var imgUrl: String = "",
    var state: BookState? = null,

    // El dueño se embebe como snapshot. Sus campos ownedBooks y reservations
    // están marcados @Transient (Spring Data) en User.kt para evitar ciclos.
    var owner: User? = null,

    // Las reservas se guardan como documentos anidados en el documento Book
    var reservations: MutableList<ReservationEmbed> = mutableListOf(),

    // Spring Data MongoDB escribe el tipo concreto en el campo _class automáticamente
    var type: BookTypeStrategy = CommonStrategy()
)

@Document(collection = "libros")
class Book(
    private val props: BookProps
) {
    // MongoDB genera automáticamente un ObjectId (String) al hacer save()
    @Id
    var id: String? = null

    var createdAt: LocalDateTime = LocalDateTime.now()

    fun calculateBookKarmaPlus(user: User): Int = getType().getBiblioKarmas(user, this)

    fun getReservedIntervals(): List<Pair<LocalDate, LocalDate>> =
        props.reservations.map { it.from to it.until }

    fun isAvailableNow() = props.reservations.none { r ->
        !r.from.isAfter(LocalDate.now()) && !r.until.isBefore(LocalDate.now())
    }

    fun isAvailableInRange(from: LocalDate, until: LocalDate): Boolean =
        props.reservations.none { r ->
            !r.from.isAfter(until) && !r.until.isBefore(from)
        }

    fun getAvailableDaysInRange(from: LocalDate, until: LocalDate): List<LocalDate> {
        val reservedDays = props.reservations
            .flatMap { r -> r.from.datesUntil(r.until.plusDays(1)).toList() }
            .toSet()
        return from.datesUntil(until.plusDays(1))
            .filter { it !in reservedDays }
            .toList()
    }

    fun addReservation(reservation: ReservationEmbed) {
        props.reservations.add(reservation)
    }

    // fun hasAnyReservation() = props.reservations.isNotEmpty()
    fun getReserved(from: LocalDate = LocalDate.now(), until: LocalDate = LocalDate.now()) = !isAvailableInRange(from, until)
    fun getAmountReservations() = props.reservations.size
    fun getTotalPages() = props.totalPages
    fun getType(): BookTypeStrategy = props.type
    fun getIsbn() = props.isbn
    fun getLanguage() = props.language
    fun getOwner() = props.owner
    fun getOwnerName() = props.owner?.getFullName() ?: "Sin dueño"
    fun getReservations() = props.reservations
    fun getTitle() = props.title
    fun getAuthor() = props.author
    fun getGenre() = props.genre
    fun getState() = props.state ?: BookState.BUENO
    fun getDescription() = props.description
    fun getEditorial() = props.editorial
    fun getPublicationDate(): LocalDate? = props.publicationDate
    fun getImgUrl() = props.imgUrl
    fun getAddedDate() = createdAt

    fun getRating(): Double? {
        val rating = props.reservations.mapNotNull { it.rating }.average()
        return if (rating.isNaN()) null else rating
    }

    fun applyProps(updated: BookProps) {
        props.title = updated.title
        props.author = updated.author
        props.description = updated.description
        props.genre = updated.genre
        props.totalPages = updated.totalPages
        props.language = updated.language
        props.editorial = updated.editorial
        props.publicationDate = updated.publicationDate
        props.isbn = updated.isbn
        props.state = updated.state
        props.type = updated.type
        props.imgUrl = updated.imgUrl
    }

    fun getBiblioKarmas(reader: User, from: LocalDate = LocalDate.now(), until: LocalDate = LocalDate.now()): Int {
        var days = ChronoUnit.DAYS.between(from, until).toInt() + 1
        if (days < 0) days = 0
        if (days == 0) days = 1
        return days * 5 + getType().getBiblioKarmas(reader, this)
    }
}
