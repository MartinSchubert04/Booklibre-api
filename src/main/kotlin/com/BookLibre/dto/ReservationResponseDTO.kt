package com.BookLibre.dto

import com.BookLibre.domain.BookState
import com.BookLibre.domain.Reservation
import java.time.LocalDate

data class ReservationResponseDTO(
    val id: Int?,
    val estado: BookState,
    val titulo: String,
    val calificacion: Double?,
    val myCalificacion: Double?,
    val autor: String,
    val prestadoPor: String? = null,
    val prestadoA: String? = null,
    val genero: String,
    val karmaGanado: Int,
    val from: LocalDate,
    val until: LocalDate,
    val imgUrl: String? = null
)

data class ReservationRatingDTO(
    val rating: Double,
    val comment: String?
)

fun Reservation.toDTO(
    bookRating: Double? = null,
    myCalificacion: Double? = null
): ReservationResponseDTO {
    return ReservationResponseDTO(
        id            = this.id,
        estado        = this.book?.getState() ?: BookState.BUENO,
        titulo        = this.bookTitle,
        calificacion  = bookRating,
        myCalificacion = myCalificacion,
        autor         = this.bookAuthor,
        prestadoPor   = this.owner.getFullName(),
        prestadoA     = this.reader.getFullName(),
        genero        = this.book?.getGenre() ?: "",
        // El objeto book se asigna en memoria al crear la reserva; fuera de ese contexto es null
        karmaGanado   = this.book?.calculateBookKarmaPlus(this.owner) ?: 0,
        from          = this.from,
        until         = this.until,
        imgUrl        = this.bookImgUrl
    )
}
