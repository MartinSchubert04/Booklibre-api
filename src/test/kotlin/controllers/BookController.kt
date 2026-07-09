package com.BookLibre.test.controller

import com.BookLibre.App
import com.BookLibre.domain.Book
import com.BookLibre.domain.BookFilter
import com.BookLibre.domain.BookProps
import com.BookLibre.domain.BookState
import com.BookLibre.domain.BookTypeStrategy
import com.BookLibre.domain.DateRange
import com.BookLibre.domain.DedicationStrategy
import com.BookLibre.domain.PageRange
import com.BookLibre.domain.User
import com.BookLibre.domain.UserProps
import com.BookLibre.dto.BookCardDTO
import com.BookLibre.dto.BookCreateDTO
import com.BookLibre.dto.BookDetailDTO
import com.BookLibre.dto.BookUpdateDTO
import com.BookLibre.dto.ReservationRequestDTO
import com.BookLibre.dto.ReviewDTO
import com.BookLibre.dto.SearchRequest
import com.BookLibre.dto.SearchResponse
import com.BookLibre.error.BusinessException
import com.BookLibre.error.NotFoundException
import com.BookLibre.repository.BookRepository
import com.BookLibre.service.BookService
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.hateoas.server.mvc.MvcLink.on
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.mockito.kotlin.*
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import java.time.LocalDate
@SpringBootTest(classes = [App::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(BookControllerTest.BootstrapSkipConfig::class)
@DisplayName("Dado el BookController")
class BookControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var mapper: ObjectMapper

    @MockBean
    lateinit var bookService: BookService

    @MockBean
    lateinit var bookRepository: BookRepository

    @TestConfiguration
    open class BootstrapSkipConfig {
        @Autowired
        lateinit var bookRepository: BookRepository

        @PostConstruct
        fun skipBootstrap() {
            Mockito.`when`(bookRepository.count()).thenReturn(1L)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun makeBook(id: String = "1"): Book {

        return Book(
            BookProps(
                title = "El Principito",
                author = "Saint-Exupéry",
                genre = "Ficción",
                language = "Español",
                type = DedicationStrategy(),
                state = BookState.EXCELENTE,
                imgUrl = "https://img.com/book.jpg"
            )
        ).also { it.id = id }
    }

    private fun makeCardDTO(id: String = "1") = BookCardDTO(
        id = id,
        title = "El Principito",
        type = "Con Dedicatoria",
        genre = "Ficción",
        author = "Saint-Exupéry",
        language = "Español",
        state = BookState.EXCELENTE,
        reserved = false,
        imgUrl = "https://img.com/book.jpg",
        bibliokarmas = 0
    )


    private fun makeSearchRequest() = SearchRequest(
        bookName = "El Principito",
        userId = 1,
        page = 0,
        quantity = 10,
        filter = BookFilter(
            genres = mutableListOf(""),
            pageRange = PageRange(2, 1000),
            dateRange = DateRange( LocalDate.now(), LocalDate.now()),
            isbn = "1234512345123",
            owner = "bob"
        ),
        sortType = null
    )

    private fun makeCreateDTO(type: String? = "FISICO") = BookCreateDTO(
        title = "El Principito",
        type = type,
        description = "Un clásico",
        genre = "Ficción",
        author = "Saint-Exupéry",
        totalPages = 100,
        isbn = "978-3-16-148410-0",
        language = "Español",
        publicationDate = LocalDate.parse("1943-04-06"),
        state = BookState.EXCELENTE,
        editorial = "Salamandra",
        imgUrl = "https://img.com/book.jpg"
    )

    @Test
    fun `PUT update devuelve 200`() {

        val json = """
    {
      "title": "Nuevo título",
      "genre": "Ficción",
      "author": "Borges",
      "description": "desc",
      "language": "Español",
      "publicationDate": "1949-01-01",
      "editorial": "Sur",
      "state": "BUENO",
      "totalPages": 100,
      "isbn": "123456789",
      "imgUrl": "null",
      "type": "Con dedicatoria"
    }
    """

        val expectedDto = BookUpdateDTO(
            title = "Nuevo título",
            description = "desc",
            type = "Con dedicatoria",
            genre = "Ficción",
            totalPages = 100,
            editorial = "Sur",
            publicationDate = LocalDate.of(1949, 1, 1),
            author = "Borges",
            isbn = "123456789",
            language = "Español",
            state = BookState.BUENO,
            imgUrl = "null"
        )

        whenever(bookService.update(any(), any())).thenReturn(expectedDto)

        mockMvc.perform(
            put("/api/book/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isOk())
    }

    @Test
    fun `PUT update devuelve 400 cuando falta title`() {

        val json = """
        {
          "genre": "Ficción"
        }
        """

        mockMvc.perform(
            put("/api/book/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isBadRequest)
    }

    @Nested
    inner class Search {

        @Test
        fun `retorna 200 y los libros cuando la busqueda es exitosa`() {
            val response = SearchResponse(listOf(makeCardDTO()), totalPages = 1)
            whenever(bookService.search(any())) doReturn response

            mockMvc.post("/api/book/search") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(makeSearchRequest())
            }.andExpect {
                status { isOk() }
                jsonPath("$.books[0].title") { value("El Principito") }
                jsonPath("$.books[0].type") { value("Con Dedicatoria") }
                jsonPath("$.totalPages") { value(1) }
            }
        }

        @Test
        fun `retorna 200 con lista vacia cuando no hay resultados`() {
            whenever(bookService.search(any())) doReturn SearchResponse(emptyList(), totalPages = 0)

            mockMvc.post("/api/book/search") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(makeSearchRequest())
            }.andExpect {
                status { isOk() }
                jsonPath("$.books") { isEmpty() }
                jsonPath("$.totalPages") { value(0) }
            }
        }

        @Test
        fun `invoca al servicio exactamente una vez`() {
            whenever(bookService.search(any())) doReturn SearchResponse(emptyList(), 0)

            mockMvc.post("/api/book/search") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(makeSearchRequest())
            }

            verify(bookService, times(1)).search(any())
        }

        @Test
        fun `retorna 404 cuando el usuario no existe`() {
            whenever(bookService.search(any())) doThrow NotFoundException("User not found")

            mockMvc.post("/api/book/search") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(makeSearchRequest())
            }.andExpect {
                status { isNotFound() }
            }
        }
    }

    // ── DELETE /api/book/{id} ─────────────────────────────────────────────────

    @Nested
    inner class Delete {

        @Test
        fun `retorna 200 cuando el libro se elimina correctamente`() {
            doNothing().whenever(bookService).delete("1")

            mockMvc.delete("/api/book/1")
                .andExpect { status { isOk() } }
        }

        @Test
        fun `invoca al servicio con el id correcto`() {
            doNothing().whenever(bookService).delete("42")

            mockMvc.delete("/api/book/42")
                .andExpect { status { isOk() } }

            verify(bookService, times(1)).delete("42")
        }

        @Test
        fun `retorna 404 cuando el libro no existe`() {
            whenever(bookService.delete("99")) doThrow NotFoundException("Book to delete not found")

            mockMvc.delete("/api/book/99")
                .andExpect { status { isNotFound() } }
        }

        @Test
        fun `retorna 400 cuando el libro no tiene dueno`() {
            whenever(bookService.delete("5")) doThrow BusinessException("Book to delete has no user")

            mockMvc.delete("/api/book/5")
                .andExpect { status { isBadRequest() } }
        }
    }

    // ── POST /api/book/create ─────────────────────────────────────────────────

    @Nested
    inner class Create {
        val userId = 1

        @Test
        fun `retorna 200 y el libro creado con datos validos`() {

            whenever(bookService.create(any(), eq(userId))).thenReturn(makeBook())

            mockMvc.post("/api/book/user/${userId}") {
                contentType = MediaType.APPLICATION_JSON
                content     = mapper.writeValueAsString(makeCreateDTO())
            }.andExpect {
                status { isOk() }
            }
        }

        @Test
        fun `invoca al servicio con el DTO correcto`() {
            whenever(bookService.create(any(), eq(userId))).thenReturn(makeBook())

            mockMvc.post("/api/book/user/${userId}") {
                contentType = MediaType.APPLICATION_JSON
                content     = mapper.writeValueAsString(makeCreateDTO())
            }
            verify(bookService, times(1)).create(any(), eq(1))
        }

        @Test
        fun `retorna 400 cuando el tipo de libro no existe`() {
            whenever(bookService.create(any(), eq(userId))).thenThrow(BusinessException("Type to create not found"))

            mockMvc.post("/api/book/user/${userId}") {
                contentType = MediaType.APPLICATION_JSON
                content     = mapper.writeValueAsString(makeCreateDTO(type = "INVALIDO"))
            }.andExpect {
                status { isBadRequest() }
            }
        }
    }

    // ── POST /api/book/{id}/reserve/{userId} ──────────────────────────────────

    @Nested
    inner class Reserve {
        private val bookId = "1"
        private val userId = 2

        private fun makeReservationRequest() = ReservationRequestDTO(
            fechaDesde = "2025-06-01",
            fechaHasta = "2025-06-08"
        )

        @Test
        fun `retorna 200 con bibliokarmas actualizados cuando la reserva es exitosa`() {
            whenever(bookService.createReservation(eq(bookId), eq(userId), any()))
                .thenReturn(mapOf(
                    "mensaje" to "Reserva confirmada exitosamente",
                    "bibliokarmasActuales" to 150
                ))

            mockMvc.post("/api/book/${bookId}/reserve/${userId}") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(makeReservationRequest())
            }.andExpect {
                status { isOk() }
                jsonPath("$.mensaje") { value("Reserva confirmada exitosamente") }
                jsonPath("$.bibliokarmasActuales") { value(150) }
            }
        }

        @Test
        fun `retorna 400 cuando el libro no esta disponible en esas fechas`() {
            whenever(bookService.createReservation(eq(bookId), eq(userId), any()))
                .thenThrow(BusinessException("El libro ya se encuentra reservado en esas fechas."))

            mockMvc.post("/api/book/${bookId}/reserve/${userId}") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(makeReservationRequest())
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.error") { value("BUSINESS_ERROR") }
            }
        }

        @Test
        fun `retorna 404 cuando el libro no existe`() {
            whenever(bookService.createReservation(eq("99"), eq(userId), any()))
                .thenThrow(NotFoundException("No se encontró el libro de id <99>"))

            mockMvc.post("/api/book/99/reserve/${userId}") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(makeReservationRequest())
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("NOT_FOUND") }
            }
        }
    }

    // ── GET /api/book/getDetailById/{id}/user/{userId} ────────────────────────

    @Nested
    inner class Detail {

        private fun makeDetailDTO() = BookDetailDTO(
            id = "1",
            title = "El Principito",
            description = "Un clásico atemporal",
            type = "Con Dedicatoria",
            genre = "Ficción",
            totalPages = 96,
            editorial = "Salamandra",
            publicationDate = LocalDate.of(1943, 4, 6),
            author = "Saint-Exupéry",
            isbn = "978-3-16-148410-0",
            language = "Español",
            state = BookState.EXCELENTE,
            ranking = 4.5,
            bibliokarmas = 25,
            recentReviews = listOf(
                ReviewDTO("Alice", "Hermoso", 5.0)
            ),
            imgUrl = "https://img.com/book.jpg"
        )

        @Test
        fun `retorna 200 con el detalle del libro`() {
            whenever(bookService.getBookDetail("1", 2)).thenReturn(makeDetailDTO())

            mockMvc.perform(get("/api/book/getDetailById/1/user/2"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.title").value("El Principito"))
                .andExpect(jsonPath("$.ranking").value(4.5))
                .andExpect(jsonPath("$.bibliokarmas").value(25))
                .andExpect(jsonPath("$.recentReviews.length()").value(1))
                .andExpect(jsonPath("$.recentReviews[0].reviewerName").value("Alice"))
        }

        @Test
        fun `retorna 404 cuando el libro no existe`() {
            whenever(bookService.getBookDetail("99", 2))
                .thenThrow(NotFoundException("No se encontró el libro de id <99>"))

            mockMvc.perform(get("/api/book/getDetailById/99/user/2"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
        }
    }
}