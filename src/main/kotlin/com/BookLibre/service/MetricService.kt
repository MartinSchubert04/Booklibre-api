package com.BookLibre.service

import com.BookLibre.domain.BookTypeAverageRating
import com.BookLibre.domain.CatalogHealth
import com.BookLibre.domain.ConfirmedReservationEvent
import com.BookLibre.domain.ConversionRow
import com.BookLibre.domain.Leaderboard
import com.BookLibre.domain.LeaderboardRow
import com.BookLibre.domain.NewBookEvent
import com.BookLibre.repository.BookRepository
import com.BookLibre.repository.ReservationRepository
import com.BookLibre.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class MetricService(
    private val userRepository: UserRepository,
    private val bookRepository: BookRepository,
    private val reservationRepository: ReservationRepository,
    private val clickService: ClickService,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {
    companion object {
        const val LEADERBOARD_KEY = "leaderboard:top5"
        const val CATHALOG_HEALTH_KEY = "catalog:health"
        const val LEADERBOARD_TTL_HOURS = 1L
    }

    fun leaderboard(): Leaderboard {
        val cached = redisTemplate.opsForValue().get(LEADERBOARD_KEY)
        if (cached != null) {
            val rows = objectMapper.readValue<List<LeaderboardRow>>(cached.toString())
            return Leaderboard(rows)
        }

        val rows = userRepository.findTop5ByOrderByPropsBiblioKarmasDesc()
            .map { LeaderboardRow(it.getFullName(), it.getBiblioKarmas()) }

        redisTemplate.opsForValue().set(
            LEADERBOARD_KEY,
            objectMapper.writeValueAsString(rows),
            LEADERBOARD_TTL_HOURS,
            TimeUnit.HOURS
        )

        return Leaderboard(rows)
    }

    fun conversionRate(): List<ConversionRow> {
        val topClicked = clickService.getTopWithScores(5)
        if (topClicked.isEmpty()) return emptyList()

        val reservasPorLibro: Map<String, Int> = reservationRepository
            .countReservasByBookIds(topClicked.map { it.bookId })
            .associate { (it[0] as String) to (it[1] as Long).toInt() }


        return topClicked.map { tc ->
            val reservas = reservasPorLibro[tc.bookId] ?: 0
            val tasa = if (tc.clicks > 0) reservas.toDouble() / tc.clicks else 0.0
            ConversionRow(
                bookId = tc.bookId,
                title = tc.title,
                clicks = tc.clicks,
                reservas = reservas,
                tasaConversion = tasa
            )
        }
    }
    fun activityFeed(): List<Any> {
        val books = bookRepository.findTop5ByOrderByCreatedAtDesc()
            .map { NewBookEvent(
                id = it.id ?: "",
                fecha = it.createdAt.toString(),
                tipoEvento = "LIBRO_NUEVO",
                titulo = it.getTitle(),
                usuario = it.getOwnerName()
            )}

        val reservations = reservationRepository.findRecentWithReader(PageRequest.of(0, 5))
            .map { ConfirmedReservationEvent(
                id = it.id?.toString() ?: "",
                fecha = it.from.atStartOfDay().toString(),
                tipoEvento = "RESERVA_CONFIRMADA",
                titulo = it.bookTitle,
                usuario = it.reader.getFullName()
            )}

        return (books + reservations)
            .sortedByDescending { when (it) {
                is NewBookEvent -> it.fecha
                is ConfirmedReservationEvent -> it.fecha
                else -> ""
            }}
            .take(5)
    }

    fun catalogHealth(): CatalogHealth {
        val cached = redisTemplate.opsForValue().get(CATHALOG_HEALTH_KEY)
        if (cached != null) {
            return objectMapper.readValue(cached.toString(), CatalogHealth::class.java)
        }

        val today = java.time.LocalDate.now()

        val prestados = bookRepository.countPrestados(today).toInt()
        val nuncaReservados = bookRepository.countNuncaReservados().toInt()
        val devueltos = bookRepository.countDevueltos(today).toInt()
        val reservadosAFuturo = bookRepository.countReservadosAFuturo(today).toInt()
        val total = bookRepository.count().toInt()

        val healthResult = CatalogHealth(
            total = total,
            prestados = prestados,
            disponiblesNuncaReservados = nuncaReservados,
            disponiblesReservadosAFuturo = reservadosAFuturo,
            disponiblesDevueltos = devueltos
        )

        redisTemplate.opsForValue().set(
            CATHALOG_HEALTH_KEY,
            objectMapper.writeValueAsString(healthResult),
            LEADERBOARD_TTL_HOURS,
            TimeUnit.MINUTES
        )

        return healthResult
    }



    fun averageRatingByBookType(): List<BookTypeAverageRating> {
        return bookRepository.aggregateAverageRatingByType()
            .map { raw ->
                BookTypeAverageRating(
                    raw.type,
                    raw.averageRating)
            }
    }
}