package controllers

import com.BookLibre.App
import com.BookLibre.domain.Book
import com.BookLibre.domain.BookProps
import com.BookLibre.domain.BookState
import com.BookLibre.domain.CommonStrategy
import com.BookLibre.domain.Reservation
import com.BookLibre.domain.User
import com.BookLibre.domain.UserProps
import com.BookLibre.repository.BookRepository
import com.BookLibre.repository.ReservationRepository
import com.BookLibre.repository.UserRepository
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDate

//@SpringBootTest(classes = [App::class])
//@AutoConfigureMockMvc
//class UserControllerTest @Autowired constructor(
//    private val mockMvc: MockMvc,
//    private val userRepository: UserRepository,
//    private val bookRepository: BookRepository,
//    private val reservationRepository: ReservationRepository
//) : DescribeSpec({
//
//    beforeEach {
        // Limpiar todos los repositorios y resetear contadores
//        userRepository.deleteAll()
//
//        bookRepository.deleteAll()
//
//        reservationRepository.collection.clear()
//        reservationRepository.currentId = 1
//
//        // Crear usuarios
//        val readerProps = UserProps().apply {
//            fullName = "Alice Reader"
//            email = "alice.reader@example.com"
//            phone = "+541112345678"
//            city = "Ciudad A"
//            userType = listOf(UserType.READER) as MutableList<UserType>?
//        }
//        val alice = User(readerProps)
//        userRepository.create(alice)
//
//        val publisherProps = UserProps().apply {
//            fullName = "Bob Publisher"
//            email = "bob.publisher@example.com"
//            phone = "+541100112233"
//            city = "Ciudad C"
//            userType = listOf(UserType.PUBLISHER) as MutableList<UserType>?
//        }
//        val bob = User(publisherProps)
//        userRepository.create(bob)
//
//        // Crear libros
//        val book1Props = BookProps().apply {
//            title = "El Aleph"
//            genre = "Ficción"
//            author = "Jorge Luis Borges"
//            description = "Libro de cuentos"
//            language = "Español"
//            publicationDate = "1949"
//            state = BookState.BUENO
//            totalPages = 146
//            isbn = "9780307476463"
//            type = CommonStrategy()
//            owner = bob
//        }
//        val book1 = Book(book1Props)
//        bookRepository.create(book1)
//
//        // Crear reserva para alice pidiendo prestado el libro de bob
//        val reservation1 = Reservation(
//            owner = bob,
//            reader = alice,
//            book = book1,
//            from = LocalDate.now().plusDays(1),
//            until = LocalDate.now().plusDays(8),
//            comment = null,
//            rating = null
//        )
//
//        reservationRepository.create(reservation1)
//    }
//
//    describe("GET /api/user/getAll") {
//        it("retorna lista de usuarios con status 200") {
//            mockMvc.get("/api/user/getAll")
//                .andExpect {
//                    status { isOk() }
//                }
//        }
//    }

    /*describe("GET /api/user/{id}/reservations/made") {
        it("retorna reservas hechas por el usuario avec los datos correctos") {
            mockMvc.get("/api/user/1/reservations/made")
                .andExpect {
                    status { isOk() }
                    jsonPath("$") { isArray() }
                    jsonPath("$.length()") { value(1) }
                    jsonPath("$[0].titulo") { value("El Aleph") }
                    jsonPath("$[0].autor") { value("Jorge Luis Borges") }
                    jsonPath("$[0].genero") { value("Ficción") }
                    jsonPath("$[0].estado") { value("BUENO") }
                    jsonPath("$[0].prestadoPor") { value("Bob Publisher") }
                    jsonPath("$[0].karmaGanado") { isNumber() }
                }
        }
    }

    describe("GET /api/user/{id}/reservations/received") {
        it("retorna reservas recibidas por el usuario avec los datos correctos") {
            mockMvc.get("/api/user/2/reservations/received")
                .andExpect {
                    status { isOk() }
                    jsonPath("$") { isArray() }
                    jsonPath("$.length()") { value(1) }
                    jsonPath("$[0].titulo") { value("El Aleph") }
                    jsonPath("$[0].autor") { value("Jorge Luis Borges") }
                    jsonPath("$[0].genero") { value("Ficción") }
                    jsonPath("$[0].estado") { value("BUENO") }
                    jsonPath("$[0].prestadoA") { value("Alice Reader") }
                    jsonPath("$[0].karmaGanado") { isNumber() }
                }
        }
    }*/
//})
