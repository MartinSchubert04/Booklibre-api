package com.BookLibre.controller

import com.BookLibre.App
import com.BookLibre.domain.Role
import com.BookLibre.domain.User
import com.BookLibre.domain.UserProps
import com.BookLibre.dto.ReservationResponseDTO
import com.BookLibre.dto.UserProfileDTO
import com.BookLibre.error.BusinessException
import com.BookLibre.error.NotFoundException
import com.BookLibre.repository.BookRepository
import com.BookLibre.repository.RoleRepository
import com.BookLibre.repository.UserRepository
import com.BookLibre.service.UserService
import jakarta.annotation.PostConstruct
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.mockito.Mockito
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
import kotlin.collections.mutableListOf

@SpringBootTest(classes = [App::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(UserControllerTest.BootstrapSkipConfig::class)
@DisplayName("Dado el UserController")
class UserControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var userService: UserService

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

    val reader = Role().apply { id = 1; name = "READER" }
    val publisher = Role().apply { id = 2; name = "PUBLISHER" }

   /* @Test
    fun `POST login devuelve id del usuario y el token`() {
        val json = """
        {
          "email": "test@mail.com",
          "password": "Messielmejor"
        }
        """
      //  whenever(userService.login(any())).thenReturn(Pair(1, "fake-jwt-token"))

        mockMvc.perform(
            post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(json)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.token").value("fake-jwt-token")) // Chequeamos que el JSON tenga el token
    }*/

    @Test
    fun `POST login devuelve 400 cuando credenciales incorrectas`() {
        val json = """
        {
          "email": "maria.lopez@email.com",
          "password": "incorrecta"
        }
        """

        whenever(userService.login(any()))
            .thenThrow(BusinessException("Usuario y/o contraseña incorrectos"))

        mockMvc.perform(
            post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(json)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET getAll devuelve 2 usuarios`() {
        val user1 = Mockito.mock(UserProfileDTO::class.java)
        val user2 = Mockito.mock(UserProfileDTO::class.java)

        whenever(userService.getAll()).thenReturn(listOf(user1, user2))

        mockMvc.perform(get("/api/user/getAll"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    val registerJson = """
    {
      "fullName": "Register test",
      "email": "register@email.com",
      "password": "Password123",
      "passConfirm": "Password123"
    }
    """

    @Test
    fun `Register agrega un usuario y devuelve 201`() {
        val mockUser = User(
            UserProps(
                fullName = "Alice Reader",
                email = "alice@example.com",
                phone = "+123456789",
                city = "Test City",
                biblioKarmas = 100
            )
        ).apply {
            id = 1
            setRoles(mutableSetOf(reader))
        }

        whenever(userService.register(any())).thenReturn(mockUser)

        mockMvc.perform(
            post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(registerJson)
        )
            .andExpect(status().isCreated)
    }

    @Test
    fun `Register con email repetido devuelve 400`() {
        whenever(userService.register(any()))
            .thenThrow(BusinessException("Email ya en uso"))

        mockMvc.perform(
            post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(registerJson)
        )
            .andExpect(status().isBadRequest)
    }

    // ── GET /api/user/{userId}/reservations/made ──────────────────────────────

    @Test
    fun `GET reservations made devuelve 200 con la lista de reservas`() {
        val res1 = Mockito.mock(ReservationResponseDTO::class.java)
        val res2 = Mockito.mock(ReservationResponseDTO::class.java)

        whenever(userService.getReservationsMade(1)).thenReturn(listOf(res1, res2))

        mockMvc.perform(get("/api/user/1/reservations/made"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `GET reservations made devuelve lista vacia cuando el usuario no tiene reservas`() {
        whenever(userService.getReservationsMade(1)).thenReturn(emptyList())

        mockMvc.perform(get("/api/user/1/reservations/made"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(0))
    }

    // ── GET /api/user/{userId}/reservations/received ──────────────────────────

    @Test
    fun `GET reservations received devuelve 200 con la lista de reservas`() {
        val res1 = Mockito.mock(ReservationResponseDTO::class.java)

        whenever(userService.getReservationsReceived(2)).thenReturn(listOf(res1))

        mockMvc.perform(get("/api/user/2/reservations/received"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
    }
}