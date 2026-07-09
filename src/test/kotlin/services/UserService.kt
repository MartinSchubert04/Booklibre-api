package com.BookLibre.service

import com.BookLibre.domain.User
import com.BookLibre.repository.UserRepository
import com.BookLibre.dto.RegisterParams
import com.BookLibre.error.BusinessException
import com.BookLibre.repository.BookRepository
import com.BookLibre.repository.ClickLogRepository
import com.BookLibre.repository.ReservationRepository
import com.BookLibre.repository.RoleRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.Mockito.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.doReturn
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

@DisplayName("Dado el UserService")
class UserServiceTest {
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val passwordEncoder: PasswordEncoder = mock(PasswordEncoder::class.java)
    private val reservationRepository: ReservationRepository = mock(ReservationRepository::class.java)
    private val roleRepository: RoleRepository = mock(RoleRepository::class.java)
    private val jwtService: JwtService = mock(JwtService::class.java)
    private val bookRepository: BookRepository = mock(BookRepository::class.java)
    private val clickLogRepository: ClickLogRepository = mock(ClickLogRepository::class.java)

    private val userService = UserService(userRepository, passwordEncoder, reservationRepository, roleRepository, jwtService, bookRepository, clickLogRepository)

    private val registerParams = RegisterParams(
        fullName = "test",
        email = "maria.lopez@email.com",
        password = "incorrecta",
        passConfirm = "incorrecta"
    )

    @Test
    fun `register llama a save exactamente una vez`() {
        whenever(userRepository.findByProps_Email(registerParams.email)).thenReturn(null)
        whenever(passwordEncoder.encode(anyString())).thenReturn("hashedPassword")
        whenever(userRepository.save(any<User>())).thenAnswer { invocation ->
            val user = invocation.getArgument<User>(0)
            user.id = 1
            user
        }
        whenever(userRepository.findById(1)).thenReturn(Optional.of(mock(User::class.java)))

        userService.register(registerParams)

        verify(userRepository).save(any())
    }

    @Test
    fun `register con email repetido NO llama a save`() {
        val existingUser = mock(User::class.java)
        whenever(userRepository.findByProps_Email(registerParams.email))
            .thenReturn(existingUser)

        assertThrows<BusinessException> {
            userService.register(registerParams)
        }

        verify(userRepository, never()).save(any())
    }
}
