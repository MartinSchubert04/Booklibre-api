package com.BookLibre.domain

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate

class BookTest : DescribeSpec({

    // ── Helpers ──────────────────────────────────────────────────────────────

    fun makeBook(reservations: MutableList<ReservationEmbed> = mutableListOf()): Book {
        val type = mockk<BookTypeStrategy>()
        every { type.getBiblioKarmas(any(), any()) } returns 10

        val props = BookProps(
            title = "El Señor de los Anillos",
            author = "Tolkien",
            genre = "Fantasía",
            totalPages = 500,
            type = type,
            state = BookState.EXCELENTE,
            reservations = reservations,
            imgUrl = "https://img.com/book.jpg"
        )
        return Book(props)
    }

    fun makeReservation(
        from: LocalDate,
        until: LocalDate,
        rating: Double? = null
    ): ReservationEmbed {
        return ReservationEmbed(from = from, until = until, rating = rating)
    }

    // ── isAvailableNow ────────────────────────────────────────────────────────

    describe("isAvailableNow") {

        it("retorna true cuando no hay reservas") {
            val book = makeBook()
            book.isAvailableNow().shouldBeTrue()
        }

        it("retorna false cuando hay una reserva activa hoy") {
            val today = LocalDate.now()
            val reservation = makeReservation(today.minusDays(1), today.plusDays(1))
            val book = makeBook(mutableListOf(reservation))
            book.isAvailableNow().shouldBeFalse()
        }

        it("retorna true cuando la reserva terminó ayer") {
            val yesterday = LocalDate.now().minusDays(1)
            val reservation = makeReservation(yesterday.minusDays(5), yesterday)
            val book = makeBook(mutableListOf(reservation))
            book.isAvailableNow().shouldBeTrue()
        }

        it("retorna true cuando la reserva empieza mañana") {
            val tomorrow = LocalDate.now().plusDays(1)
            val reservation = makeReservation(tomorrow, tomorrow.plusDays(3))
            val book = makeBook(mutableListOf(reservation))
            book.isAvailableNow().shouldBeTrue()
        }
    }

    // ── isAvailableInRange ────────────────────────────────────────────────────

    describe("isAvailableInRange") {

        it("retorna true cuando no hay reservas") {
            val book = makeBook()
            val from = LocalDate.now().plusDays(5)
            val until = LocalDate.now().plusDays(10)
            book.isAvailableInRange(from, until).shouldBeTrue()
        }

        it("retorna false cuando el rango se solapa con una reserva existente") {
            val base = LocalDate.now().plusDays(5)
            val reservation = makeReservation(base, base.plusDays(5))
            val book = makeBook(mutableListOf(reservation))

            // rango parcialmente solapado
            book.isAvailableInRange(base.plusDays(3), base.plusDays(8)).shouldBeFalse()
        }

        it("retorna false cuando el rango queda completamente dentro de una reserva") {
            val base = LocalDate.now().plusDays(1)
            val reservation = makeReservation(base, base.plusDays(10))
            val book = makeBook(mutableListOf(reservation))

            book.isAvailableInRange(base.plusDays(2), base.plusDays(4)).shouldBeFalse()
        }

        it("retorna true cuando el rango es justo anterior a una reserva existente") {
            val reservationStart = LocalDate.now().plusDays(10)
            val reservation = makeReservation(reservationStart, reservationStart.plusDays(5))
            val book = makeBook(mutableListOf(reservation))

            val from = LocalDate.now().plusDays(1)
            val until = reservationStart.minusDays(1)
            book.isAvailableInRange(from, until).shouldBeTrue()
        }

        it("retorna true cuando el rango empieza justo después de que termina la reserva") {
            val reservationEnd = LocalDate.now().plusDays(5)
            val reservation = makeReservation(LocalDate.now().plusDays(1), reservationEnd)
            val book = makeBook(mutableListOf(reservation))

            book.isAvailableInRange(reservationEnd.plusDays(1), reservationEnd.plusDays(5)).shouldBeTrue()
        }
    }

    // ── getAvailableDaysInRange ───────────────────────────────────────────────

    describe("getAvailableDaysInRange") {

        it("retorna todos los días cuando no hay reservas") {
            val book = makeBook()
            val from = LocalDate.now().plusDays(1)
            val until = LocalDate.now().plusDays(3)
            val days = book.getAvailableDaysInRange(from, until)
            days shouldHaveSize 3
        }

        it("excluye los días reservados") {
            val base = LocalDate.now().plusDays(1)
            val reservation = makeReservation(base.plusDays(1), base.plusDays(1))
            val book = makeBook(mutableListOf(reservation))

            val days = book.getAvailableDaysInRange(base, base.plusDays(2))
            days shouldHaveSize 2
            days shouldContain base
            days shouldContain base.plusDays(2)
        }

        it("retorna lista vacía cuando todos los días están reservados") {
            val base = LocalDate.now().plusDays(1)
            val reservation = makeReservation(base, base.plusDays(2))
            val book = makeBook(mutableListOf(reservation))

            book.getAvailableDaysInRange(base, base.plusDays(2)).shouldBeEmpty()
        }
    }

    // ── getRating ─────────────────────────────────────────────────────────────

    describe("getRating") {

        it("retorna null cuando no hay reservas con rating") {
            val book = makeBook()
            book.getRating().shouldBeNull()
        }

        it("retorna null cuando las reservas no tienen rating") {
            val res = makeReservation(LocalDate.now(), LocalDate.now().plusDays(3), rating = null)
            val book = makeBook(mutableListOf(res))
            book.getRating().shouldBeNull()
        }

        it("retorna el promedio correcto con una sola reserva calificada") {
            val res = makeReservation(LocalDate.now(), LocalDate.now().plusDays(3), rating = 4.0)
            val book = makeBook(mutableListOf(res))
            book.getRating() shouldBe 4.0
        }

        it("retorna el promedio correcto con múltiples reservas calificadas") {
            val r1 = makeReservation(LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), rating = 5.0)
            val r2 = makeReservation(LocalDate.now().minusDays(3), LocalDate.now().minusDays(1), rating = 3.0)
            val book = makeBook(mutableListOf(r1, r2))
            book.getRating() shouldBe 4.0
        }
    }

    // ── getBiblioKarmas ───────────────────────────────────────────────────────

    describe("getBiblioKarmas") {

        it("suma correctamente días * 5 más el extra del tipo") {
            // mock devuelve 10 de extra
            val book = makeBook()
            val user = mockk<User>(relaxed = true)
            val from = LocalDate.now()
            val until = LocalDate.now().plusDays(3) //3 días de diferencia matematica + 1 = 4 días reales.

            book.getBiblioKarmas(user, from, until) shouldBe 30 // 4 * 5 = 20 base + 10 extra = 30
        }

        it("usa mínimo 1 día cuando from == until") {
            val book = makeBook()
            val user = mockk<User>(relaxed = true)
            val today = LocalDate.now()

            // 0 días calculados → se fuerza a 1 → 1*5 + 10 = 15
            book.getBiblioKarmas(user, today, today) shouldBe 15
        }

        it("trata días negativos como 1 día mínimo") {
            val book = makeBook()
            val user = mockk<User>(relaxed = true)
            val from = LocalDate.now().plusDays(5)
            val until = LocalDate.now() // until < from → días negativos

            book.getBiblioKarmas(user, from, until) shouldBe 15
        }
    }

    // ── calculateTotalBiblioKarmas ────────────────────────────────────────────

    describe("calculateTotalBiblioKarmas") {

        it("calcula correctamente con fechas válidas") {
            val book = makeBook()
            val user = mockk<User>(relaxed = true)

            val result = book.calculateBookKarmaPlus(user)

            result shouldBe 10
        }
    }

    // ── getReservedIntervals ──────────────────────────────────────────────────

    describe("getReservedIntervals") {

        it("retorna lista vacía cuando no hay reservas") {
            val book = makeBook()
            book.getReservedIntervals().shouldBeEmpty()
        }

        it("retorna los pares from-until de cada reserva") {
            val from = LocalDate.now().plusDays(1)
            val until = LocalDate.now().plusDays(3)
            val res = makeReservation(from, until)
            val book = makeBook(mutableListOf(res))

            val intervals = book.getReservedIntervals()
            intervals shouldHaveSize 1
            intervals[0].first shouldBe from
            intervals[0].second shouldBe until
        }
    }

    // ── getAmountReservations ─────────────────────────────────────────────────

    describe("getAmountReservations") {

        it("retorna 0 sin reservas") {
            makeBook().getAmountReservations() shouldBe 0
        }

        it("cuenta correctamente múltiples reservas") {
            val reservations = mutableListOf(
                makeReservation(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)),
                makeReservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(7))
            )
            makeBook(reservations).getAmountReservations() shouldBe 2
        }
    }

    // ── addReservation ────────────────────────────────────────────────────────

    describe("addReservation") {

        it("agrega una reserva correctamente") {
            val book = makeBook()
            val res = makeReservation(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3))
            book.addReservation(res)
            book.getAmountReservations() shouldBe 1
        }
    }
})