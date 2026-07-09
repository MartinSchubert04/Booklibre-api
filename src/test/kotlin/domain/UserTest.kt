package com.BookLibre.domain

import com.BookLibre.error.BusinessException
import com.BookLibre.repository.RoleRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate

class UserTest : DescribeSpec({

    // ── Helpers ───────────────────────────────────────────────────────────────

    val reader = Role().apply { id = 1; name = "READER" }
    val publisher = Role().apply { id = 2; name = "PUBLISHER" }

    fun makeUser(
        fullName: String = "Juan Perez",
        email: String = "juan@mail.com",
        phone: String = "1234567890",
        city: String = "Buenos Aires",
        biblioKarmas: Int = 0,
        reservations: MutableList<Reservation> = mutableListOf(),
        ownedBooks: MutableList<Book> = mutableListOf()
    ) = User(
        UserProps(
            fullName = fullName,
            email = email,
            phone = phone,
            city = city,
            biblioKarmas = biblioKarmas
        )
    ).apply {
        setReservations(reservations)
        setOwnedBooks(ownedBooks)
        setRoles(mutableSetOf(reader))
    }

    fun makeReservation(reader: User, owner: User? = null): Reservation {
        val book = mockk<Book>(relaxed = true)
        every { book.getBiblioKarmas(any(), any(), any()) } returns 0
        val res = mockk<Reservation>(relaxed = true)
        every { res.reader } returns reader
        return res
    }

    fun makeBook(reservations: MutableList<ReservationEmbed> = mutableListOf(), availableNow: Boolean = true): Book {
        val book = mockk<Book>(relaxed = true)
        every { book.getReservations() } returns reservations
        every { book.isAvailableNow() } returns availableNow
        return book
    }

    // ── update ────────────────────────────────────────────────────────────────

    describe("update") {

        it("actualiza los campos correctamente con datos válidos") {
            val user = makeUser()
            user.update(
                fullName = "María García",
                email = "maria@mail.com",
                phone = "9876543210",
                city = "Córdoba",
                imgUrl = "https://img.com/avatar.jpg",
                description = "asd"
            )
            user.apply { setRoles(mutableSetOf(publisher)) }
            user.getFullName() shouldBe "María García"
            user.getEmail() shouldBe "maria@mail.com"
            user.getPhone() shouldBe "9876543210"
            user.getCity() shouldBe "Córdoba"
            user.getRoles() shouldBe listOf("PUBLISHER")
            user.getImgUrl() shouldBe "https://img.com/avatar.jpg"
        }

        it("lanza excepción cuando fullName está vacío") {
            val user = makeUser()
            shouldThrow<BusinessException> {
                user.update("", "mail@mail.com", "asd", "123", "BsAs", "")
            }
        }

        it("lanza excepción cuando email está vacío") {
            val user = makeUser()
            shouldThrow<BusinessException> {
                user.update("Juan", "", "asd","123", "BsAs", "")
            }
        }

        it("lanza excepción cuando phone está vacío") {
            val user = makeUser()
            shouldThrow<BusinessException> {
                user.update("Juan", "mail@mail.com", "asd","", "BsAs", "")
            }
        }

        it("lanza excepción cuando city está vacía") {
            val user = makeUser()
            shouldThrow<BusinessException> {
                user.update("Juan", "mail@mail.com", "asd","123", "", "")
            }
        }

    }

    // ── addBiblioKarmas / getBiblioKarmas ─────────────────────────────────────

    describe("addBiblioKarmas") {

        it("acumula karmas correctamente") {
            val user = makeUser(biblioKarmas = 10)
            user.addBiblioKarmas(15)
            user.getBiblioKarmas() shouldBe 25
        }

        it("acumula múltiples veces correctamente") {
            val user = makeUser(biblioKarmas = 0)
            user.addBiblioKarmas(5)
            user.addBiblioKarmas(5)
            user.getBiblioKarmas() shouldBe 10
        }

        it("parte desde 0 si no tiene karmas previos") {
            val user = makeUser()
            user.getBiblioKarmas() shouldBe 0
        }
    }

    // ── addBook / removeBook / getOwnedBooks ──────────────────────────────────

    describe("addBook y removeBook") {

        it("agrega un libro correctamente") {
            val user = makeUser()
            val book = makeBook()
            user.addBook(book)
            user.getOwnedBooks() shouldHaveSize 1
        }

        it("elimina un libro correctamente") {
            val book = makeBook()
            val user = makeUser(ownedBooks = mutableListOf(book))
            user.removeBook(book)
            user.getOwnedBooks().shouldBeEmpty()
        }

        it("no modifica otros libros al eliminar uno") {
            val book1 = makeBook()
            val book2 = makeBook()
            val user = makeUser(ownedBooks = mutableListOf(book1, book2))
            user.removeBook(book1)
            user.getOwnedBooks() shouldHaveSize 1
            user.getOwnedBooks() shouldContain book2
        }
    }

    // ── addReservation / getAmountReservations ────────────────────────────────

    describe("addReservation") {

        it("agrega una reserva correctamente") {
            val user = makeUser()
            val res = makeReservation(reader = user)
            user.addReservation(res)
            user.getAmoutReservations() shouldBe 1
        }

        it("cuenta correctamente múltiples reservas") {
            val user = makeUser()
            user.addReservation(makeReservation(reader = user))
            user.addReservation(makeReservation(reader = user))
            user.getAmoutReservations() shouldBe 2
        }

        it("retorna 0 cuando no tiene reservas") {
            makeUser().getAmoutReservations() shouldBe 0
        }
    }

    // ── getLentBooks / getLentBooksAmount ─────────────────────────────────────

    describe("getLentBooks") {

        it("retorna solo libros con al menos una reserva") {
            val bookWithRes = makeBook(reservations = mutableListOf(mockk<ReservationEmbed>(relaxed = true)))
            val bookWithoutRes = makeBook(reservations = mutableListOf())
            val user = makeUser(ownedBooks = mutableListOf(bookWithRes, bookWithoutRes))

            user.getLentBooks() shouldHaveSize 1
            user.getLentBooks() shouldContain bookWithRes
        }

        it("retorna lista vacía si ningún libro fue prestado") {
            val book = makeBook(reservations = mutableListOf())
            val user = makeUser(ownedBooks = mutableListOf(book))
            user.getLentBooks().shouldBeEmpty()
        }

        it("getLentBooksAmount cuenta correctamente") {
            val bookWithRes = makeBook(reservations = mutableListOf(mockk<ReservationEmbed>(relaxed = true)))
            val user = makeUser(ownedBooks = mutableListOf(bookWithRes))
            user.getLentBooksAmout() shouldBe 1
        }
    }

    // ── getReadedBooksAmount ──────────────────────────────────────────────────

    describe("getReadedBooksAmount") {

        it("cuenta solo las reservas donde el usuario es el reader") {
            val user = makeUser()
            val otherUser = makeUser(fullName = "Otro")

            val resAsReader = mockk<Reservation>(relaxed = true)
            every { resAsReader.reader } returns user

            val resAsOwner = mockk<Reservation>(relaxed = true)
            every { resAsOwner.reader } returns otherUser

            user.addReservation(resAsReader)
            user.addReservation(resAsOwner)

            user.getReadedBooksAmount() shouldBe 1
        }

        it("retorna 0 si nunca fue reader") {
            val user = makeUser()
            val otherUser = makeUser(fullName = "Otro")
            val res = mockk<Reservation>(relaxed = true)
            every { res.reader } returns otherUser
            user.addReservation(res)

            user.getReadedBooksAmount() shouldBe 0
        }
    }

    // ── getBooksAvailableNow ──────────────────────────────────────────────────

    describe("getBooksAvailableNow") {

        it("retorna solo los libros disponibles en este momento") {
            val available = makeBook(availableNow = true)
            val occupied = makeBook(availableNow = false)
            val user = makeUser(ownedBooks = mutableListOf(available, occupied))

            user.getBooksAvailableNow() shouldHaveSize 1
            user.getBooksAvailableNow() shouldContain available
        }

        it("retorna lista vacía si ningún libro está disponible") {
            val occupied = makeBook(availableNow = false)
            val user = makeUser(ownedBooks = mutableListOf(occupied))
            user.getBooksAvailableNow().shouldBeEmpty()
        }
    }

    // ── getReservedBooks ──────────────────────────────────────────────────────

    describe("getReservedBooks") {

        it("retorna solo las reservas donde el usuario es reader") {
            val user = makeUser()
            val otherUser = makeUser(fullName = "Otro")

            val resAsReader = mockk<Reservation>(relaxed = true)
            every { resAsReader.reader } returns user

            val resNotReader = mockk<Reservation>(relaxed = true)
            every { resNotReader.reader } returns otherUser

            user.addReservation(resAsReader)
            user.addReservation(resNotReader)

            user.getReservedBooks() shouldHaveSize 1
            user.getReservedBooks() shouldContain resAsReader
        }
    }

    // ── getReservationsFromMyBooks ────────────────────────────────────────────

    describe("getReservationsFromMyBooks") {

        it("agrega las reservas de todos los libros propios") {
            val res1 = mockk<ReservationEmbed>(relaxed = true)
            val res2 = mockk<ReservationEmbed>(relaxed = true)
            val book1 = makeBook(reservations = mutableListOf(res1))
            val book2 = makeBook(reservations = mutableListOf(res2))
            val user = makeUser(ownedBooks = mutableListOf(book1, book2))

            user.getReservationsFromMyBooks() shouldHaveSize 2
        }

        it("retorna lista vacía si ningún libro tiene reservas") {
            val book = makeBook(reservations = mutableListOf())
            val user = makeUser(ownedBooks = mutableListOf(book))
            user.getReservationsFromMyBooks().shouldBeEmpty()
        }
    }
})