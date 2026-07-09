import com.BookLibre.domain.Book
import com.BookLibre.domain.BookProps
import com.BookLibre.domain.BookState
import com.BookLibre.domain.CommonStrategy
import com.BookLibre.domain.DedicationStrategy
import com.BookLibre.domain.Reservation
import com.BookLibre.domain.ReservationEmbed
import com.BookLibre.domain.Role
import com.BookLibre.domain.User
import com.BookLibre.domain.UserProps
import com.BookLibre.repository.RoleRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class ReservationTest : DescribeSpec({

    val reader = Role().apply { id = 1; name = "READER" }
    val publisher = Role().apply { id = 2; name = "PUBLISHER" }

    val ownerProps = UserProps().apply {
        fullName = "Bob Owner"
        email = "bob@example.com"
        phone = "+987654321"
        city = "Owner City"
        biblioKarmas = 50
    }

    var owner = User(ownerProps).apply {
        setRoles(mutableSetOf(publisher))
    }
    owner.id = 2

    val commonBookProps = BookProps().apply {
        title = "Kotlin Guide"
        description = "A test book"
        genre = "Testing"
        author = "Test Author"
        totalPages = 300
        language = "English"
        publicationDate = LocalDate.of(2024, 1, 1)
        state = BookState.EXCELENTE
        type = CommonStrategy()
        owner = owner
    }
    val commonBook = Book(commonBookProps)
    commonBook.id = "1"

    val dedicationBookProps = BookProps().apply {
        title = "Rare Edition"
        description = "A rare book"
        genre = "Rare"
        author = "Famous Author"
        totalPages = 400
        language = "Spanish"
        publicationDate = LocalDate.of(2020, 1, 1)
        state = BookState.MUY_BUENO
        type = DedicationStrategy()
        owner = owner
    }
    val dedicationBook = Book(dedicationBookProps)
    dedicationBook.id = "2"

    // Helper que replica lo que hace ReservationService.createReservation()
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
            bookId = book.id ?: "",
            bookTitle = book.getTitle(),
            bookAuthor = book.getAuthor(),
            bookImgUrl = book.getImgUrl(),
            from = from,
            until = until

        ).also { it.book = book }
        val karmas = reservation.calculateBiblioKarmas()
        reader.addBiblioKarmas(karmas)
        book.addReservation(ReservationEmbed(from = from, until = until, rating = rating, comment = comment))
        owner.addReservation(reservation)
        reader.addReservation(reservation)
        return reservation
    }

    // -------------------------------------------------------

    describe("CrearReservation") {
        it("crea una reserva con fechas correctas") {
            val testReader = User(UserProps().apply {
                fullName = "Alice Reader"
                email = "alice@example.com"
                phone = "+123456789"
                city = "Test City"
                biblioKarmas = 100
            }).apply {
                setRoles(mutableSetOf(reader))
            }
            testReader.id = 1

            val from = LocalDate.of(2024, 3, 1)
            val until = LocalDate.of(2024, 3, 8)

            val reservation = createReservation(owner, testReader, commonBook, from, until, "Un clásico atemporal.", 5.0)

            reservation.reader shouldBe testReader
            reservation.book shouldBe commonBook
            reservation.from shouldBe from
            reservation.until shouldBe until
        }

        it("inicia sin ID asignado") {
            val testReader = User(UserProps().apply {
                fullName = "Alice Reader"
                email = "alice@example.com"
                phone = "+123456789"
                city = "Test City"
                biblioKarmas = 100
            }).apply {
                setRoles(mutableSetOf(reader))
            }
            testReader.id = 1

            val reservation = createReservation(
                owner, testReader, commonBook,
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 8)
            )

            reservation.id shouldBe null
        }
    }

    describe("CalcularBiblioKarmas") {
        it("incrementa karmas del lector según días de reserva") {
            val newReader = User(UserProps().apply {
                fullName = "Alice Reader"
                email = "alice@example.com"
                phone = "+123456789"
                city = "Test City"
                biblioKarmas = 100
            }).apply {
                setRoles(mutableSetOf(reader))
            }
            newReader.id = 10

            val from = LocalDate.of(2024, 3, 1)
            val to = LocalDate.of(2024, 3, 8) // 8 días

            val initialKarmas = newReader.getBiblioKarmas()
            val expectedExtra = commonBook.getType().getBiblioKarmas(newReader, commonBook)

            createReservation(owner, newReader, commonBook, from, to)

            newReader.getBiblioKarmas() shouldBe initialKarmas + 40 + expectedExtra
        }

        it("calcula karmas incluso para reserva de 1 día") {
            val newReader = User(UserProps().apply {
                fullName = "Alice Reader"
                email = "alice@example.com"
                phone = "+123456789"
                city = "Test City"
                biblioKarmas = 100
            }).apply {
                setRoles(mutableSetOf(reader))
            }
            newReader.id = 11

            val from = LocalDate.of(2024, 3, 1)
            val to = LocalDate.of(2024, 3, 2) // 2 días

            val initialKarmas = newReader.getBiblioKarmas()
            val expectedExtra = commonBook.getType().getBiblioKarmas(newReader, commonBook)

            createReservation(owner, newReader, commonBook, from, to)

            newReader.getBiblioKarmas() shouldBe initialKarmas + 10 + expectedExtra
        }

        it("cambia karmas según tipo de libro") {
            val newReader = User(UserProps().apply {
                fullName = "Alice Reader"
                email = "alice@example.com"
                phone = "+123456789"
                city = "Test City"
                biblioKarmas = 100
            }).apply {
                setRoles(mutableSetOf(reader))
            }
            newReader.id = 12

            val from = LocalDate.of(2024, 3, 1)
            val to = LocalDate.of(2024, 3, 10) // 10 días

            val initialKarmas = newReader.getBiblioKarmas()
            val days = from.until(to).days + 1
            val expectedExtra = dedicationBook.getType().getBiblioKarmas(newReader, dedicationBook)

            createReservation(owner, newReader, dedicationBook, from, to)

            newReader.getBiblioKarmas() shouldBe initialKarmas + (days * 5) + expectedExtra
        }
    }

    describe("ValidarFechasReservation") {
        it("crea reserva con dates en futuro") {
            val testReader = User(UserProps().apply {
                fullName = "Alice Reader"
                email = "alice@example.com"
                phone = "+123456789"
                city = "Test City"
                biblioKarmas = 100
            }).apply {
                setRoles(mutableSetOf(reader))
            }
            testReader.id = 1

            val from = LocalDate.now().plusDays(1)
            val to = LocalDate.now().plusDays(7)

            val reservation = createReservation(owner, testReader, commonBook, from, to)

            reservation.from shouldBe from
            reservation.until shouldBe to
        }

        it("crea reserva con diferentes estrategias de libro") {
            val readerProps = UserProps().apply {
                fullName = "Alice Reader"
                email = "alice@example.com"
                phone = "+123456789"
                city = "Test City"
                biblioKarmas = 100
            }

            val from = LocalDate.of(2024, 3, 1)
            val to = LocalDate.of(2024, 3, 5)

            val testReader1 = User(readerProps).also { it.id = 1 }.apply {
                setRoles(mutableSetOf(reader))
            }
            val reservationCommon = createReservation(owner, testReader1, commonBook, from, to)

            val testReader2 = User(readerProps).also { it.id = 13 }.apply {
                setRoles(mutableSetOf(reader))
            }
            val reservationDedication = createReservation(owner, testReader2, dedicationBook, from, to)

            reservationCommon.book?.getType() shouldBe commonBook.getType()
            reservationDedication.book?.getType() shouldBe dedicationBook.getType()
        }
    }
})