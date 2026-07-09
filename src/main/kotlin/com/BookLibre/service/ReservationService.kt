package com.BookLibre.service

import com.BookLibre.domain.Book
import com.BookLibre.domain.Reservation
import com.BookLibre.domain.ReservationEmbed
import com.BookLibre.domain.User
import com.BookLibre.dto.ReservationRatingDTO
import com.BookLibre.repository.BookRepository
import com.BookLibre.repository.ReservationRepository
import com.BookLibre.error.BusinessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val bookRepository: BookRepository
) {
    @Transactional
    fun createReservation(
        owner: User,
        reader: User,
        book: Book,
        from: LocalDate,
        until: LocalDate,
        comment: String? = null,
        rating: Double? = null
    ): Reservation {
        val reservation = Reservation(
            owner = owner,
            reader = reader,
            bookId     = book.id ?: error("El libro debe estar guardado antes de crear una reserva"),
            bookTitle  = book.getTitle(),
            bookAuthor = book.getAuthor(),
            bookImgUrl = book.getImgUrl(),
            from    = from,
            until   = until
        ).also { it.book = book }

        val karmas = reservation.calculateBiblioKarmas()
        reader.addBiblioKarmas(karmas)

        val savedReservation = reservationRepository.save(reservation)

        // Snapshot de la reserva embebido en el documento Book de MongoDB.
        // El id de la reserva (Postgres) es la clave de matcheo posterior para calificar.
        book.addReservation(
            ReservationEmbed(
                id = savedReservation.id,
                from = from,
                until = until,
                rating = rating,
                comment = comment
            )
        )
        bookRepository.save(book)

        owner.addReservation(savedReservation)
        reader.addReservation(savedReservation)

        return savedReservation
    }

    @Transactional(readOnly = true)
    fun getAll(): List<Reservation> = reservationRepository.findAll().toList()

    fun rateReservation(reservationId: Int, dto: ReservationRatingDTO) {
        if (dto.rating !in 1.0..5.0) throw BusinessException("Rating must be between 1 and 5")

        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { BusinessException("Reservation not found") }

        val book = bookRepository.findById(reservation.bookId)
            .orElseThrow { BusinessException("Book not found for reservation") }

        val embeds = book.getReservations()

        // Matcheo principal por id (reservas nuevas). Fallback por fechas para reservas
        // legacy creadas antes de que el embed guardara el id.
        val embed = embeds.find { it.id == reservationId }
            ?: embeds.find { it.id == null && it.from == reservation.from && it.until == reservation.until }
            ?: throw BusinessException("Reservation embed not found in Book")

        val updatedEmbed = embed.copy(
            id = reservationId,
            rating = dto.rating,
            comment = dto.comment
        )
        embeds.remove(embed)
        embeds.add(updatedEmbed)
        bookRepository.save(book)
    }
}